/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat, Inc.), René Groeschke (Gradle, Inc.) - Story: Integrate Eclipse proxy settings into Buildship model loading and task execution
 */

package org.eclipse.buildship.core.proxy

import org.eclipse.core.runtime.jobs.Job
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob
import org.eclipse.buildship.core.util.progress.ToolingApiJob
import org.eclipse.debug.core.ILaunch
import org.eclipse.core.net.proxy.IProxyService
import org.eclipse.core.net.proxy.IProxyData
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.launch.RunGradleConfigurationDelegateJob
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration

import org.eclipse.core.runtime.IPath
import org.eclipse.debug.core.ILaunchConfiguration

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import org.eclipse.buildship.core.proxy.support.*
import org.eclipse.buildship.core.test.fixtures.*

import org.eclipse.buildship.core.console.ProcessStreams
import org.eclipse.buildship.core.console.ProcessStreamsProvider
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import org.hamcrest.*
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification

class ProxySettingsTest extends ProjectImportSpecification {

    @Rule TemporaryFolder tempFolder
    @Rule TemporaryFolder dependencyTempFolder
    @Rule TemporaryFolder gradleHomeTempFolder
    ProcessStreamsProvider processStreamsProvider
    @Rule public final HttpServer server = new HttpServer()
    @Rule TestProxyServer proxyServer = new TestProxyServer(server)
    IProxyService proxyService

    def proxyHost = 'localhost'
    def permHost = 'permHost'
    def tempHost = 'tempHost'
    def userId = 'test-user'
    def password = 'test-password'

    def setup() {
        createTestProxyFiles()
        server.start()
        proxyService = CorePlugin.getProxyService()
        proxyService.setProxiesEnabled(true)
        proxyService.setSystemProxiesEnabled(false)
    }

    def "Eclipse proxy settings are properly collected"() {
        def proxySupporter = new EclipseProxySettingsSupporter()

        setup:
        setupTestProxyData(proxyHost, proxyServer.port, userId, password)

        when:
        proxySupporter.configureEclipseProxySettings()

        then:
        System.getProperty("http.proxyHost") == proxyHost
        System.getProperty("http.proxyPort") == proxyServer.port.toString()
        System.getProperty("http.proxyUser") == userId
        System.getProperty("http.proxyPassword") == password

        cleanup:
        proxySupporter.restoreSystemProxySettings()
    }

    def "System properties temporarily changed when ToolingApiWorkspaceJob is run"() {
        String retrievedHost

        setup:
        setupTestProxyData(tempHost, 8080, userId, password)
        System.setProperty("http.proxyHost", permHost)
        Job job = new ToolingApiWorkspaceJob("Test") {
                    @Override
                    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
                        retrievedHost = System.getProperty("http.proxyHost")
                    }
                }

        when:
        job.schedule()
        job.join()

        then:
        retrievedHost == tempHost
        System.getProperty("http.proxyHost") == permHost
    }

    def "System properties temporarily changed when ToolingApiJob is run"() {
        String retrievedHost

        setup:
        setupTestProxyData(tempHost, 0000, userId, password)
        System.setProperty("http.proxyHost", permHost)
        Job job = new ToolingApiJob("Test") {
                    @Override
                    protected void runToolingApiJob(IProgressMonitor monitor) {
                        retrievedHost = System.getProperty("http.proxyHost")
                    }
                }

        when:
        job.schedule()
        job.join()

        then:
        retrievedHost == tempHost
        System.getProperty("http.proxyHost") == permHost
    }

    //    def "JVM arguments are automatically set when Eclipse proxy settings are available"() {
    //        setup:
    //        setupTestProxyData()
    //        List<String> arguments
    //        def job = new ToolingApiJob("Test") {
    //                    @Override
    //                    protected void runToolingApiJob(IProgressMonitor monitor) {
    //                        System.getProperty("http.proxyHost")
    //                    }
    //                }
    //        when:
    //        job.schedule()
    //        job.join()
    //
    //        then:
    //        System.out.println(">> " + arguments)
    //    }

    def "Different proxy settings can be used by subsequent builds"() {
        String retrievedHost
        String secondTempHost = 'tempHost2'

        setup:
        setupTestProxyData(tempHost, 8080, userId, password)
        System.setProperty("http.proxyHost", permHost)
        def firstJob = new ToolingApiJob("Test") {
                    @Override
                    protected void runToolingApiJob(IProgressMonitor monitor) {
                    }
                }

        def secondJob = new ToolingApiJob("SecondTest") {
                    @Override
                    protected void runToolingApiJob(IProgressMonitor monitor) {
                        retrievedHost = System.getProperty("http.proxyHost")
                    }
                }

        when:
        firstJob.schedule()
        firstJob.join()

        setupTestProxyData(secondTempHost, 8080, userId, password)

        secondJob.schedule()
        secondJob.join()

        then:
        System.getProperty("http.proxyHost") == permHost
        retrievedHost == secondTempHost
    }

    def "Proxies are accessed upon Gradle distribution download"() {
        setup:
        proxyServer.start()
        proxyServer.requireAuthentication(userId, password)
        setupTestProxyData("localhost", proxyServer.port, userId, password)
        File rootProject = newProject(true, true)
        server.expectGet("/gradlew/dist", new File(dependencyTempFolder.root, 'gradle-bin.zip'))
        ProjectImportConfiguration configuration = getProjectConfiguration()
        Job job = newProjectImportJobWithConfiguration(rootProject)

        when:
        job.schedule()
        job.join()

        then:
        proxyServer.requestCount == 1
    }

    def "Proxies are accessed from Gradle build VM"() {
        setup:
        proxyServer.start()
        proxyServer.requireAuthentication(userId, password)
        setupTestProxyData("localhost", proxyServer.port, userId, password)
        File rootProject = newProject(false, true)
        server.expectGet("/not-a-real-group/not-a-real-dependency/0.0/not-a-real-dependency-0.0.pom", new File(dependencyTempFolder.root, 'not-a-real-dependency-0.0.pom'))
        def job = new RunGradleConfigurationDelegateJob(createLaunchMock(), createLaunchConfigurationMock(rootProject.absolutePath))

        when:
        job.schedule()
        job.join()

        then:
        proxyServer.requestCount == 1
    }

    def "Proxies are not accessed from Gradle build VM with incorrect proxy settings"() {
        setup:
        proxyServer.start()
        proxyServer.requireAuthentication(userId, password)
        setupTestProxyData("localhost", proxyServer.port, userId, password + '-incorrect-password')
        File rootProject = newProject(false, true)
        def job = new RunGradleConfigurationDelegateJob(createLaunchMock(), createLaunchConfigurationMock(rootProject.absolutePath))

        when:
        job.schedule()
        job.join()

        then:
        proxyServer.requestCount == 0
    }

    def setupTestProxyData(String host, int port, String userId, String password) {
        String proxyHostMemory = System.getProperty("http.proxyHost")
        String proxyPortMemory = System.getProperty("http.proxyPort")
        String proxyUserMemory = System.getProperty("http.proxyUser")
        String proxyPassMemory = System.getProperty("http.proxyPassword")

        final IProxyData httpProxyData = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE)

        httpProxyData.setHost(host)
        httpProxyData.setPort(port)
        httpProxyData.setUserid(userId)
        httpProxyData.setPassword(password)
        proxyService.setProxyData((IProxyData[]) [
            httpProxyData
        ])

        if (proxyHostMemory != null) {
            System.setProperty("http.proxyHost", proxyHostMemory)
        }
        if (proxyPortMemory != null) {
            System.setProperty("http.proxyPort", proxyPortMemory)
        }
        if (proxyUserMemory != null) {
            System.setProperty("http.proxyUser", proxyUserMemory)
        }
        if (proxyPassMemory != null) {
            System.setProperty("http.proxyPassword", proxyPassMemory)
        }

    }

    def newProject(boolean projectDescriptorExists, boolean applyJavaPlugin) {
        def root = tempFolder.newFolder('simple-project')
        new File(root, 'build.gradle') << (applyJavaPlugin ? '''apply plugin: "java"
repositories { maven { url "http://not.a.real.domain" } }
dependencies { compile "not-a-real-group:not-a-real-dependency:0.0" }''' : '')
        new File(root, 'settings.gradle') << ''
        new File(root, 'src/main/java').mkdirs()

        if (!projectDescriptorExists) {
            new File(root, '.project') << '''<?xml version="1.0" encoding="UTF-8"?>
                <projectDescription>
                  <name>simple-project</name>
                  <comment>original</comment>
                  <projects></projects>
                  <buildSpec></buildSpec>
                  <natures></natures>
                </projectDescription>'''
            if (applyJavaPlugin) {
                new File(root, '.classpath') << '''<?xml version="1.0" encoding="UTF-8"?>
                    <classpath>
                      <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
                      <classpathentry kind="src" path="src/main/java"/>
                      <classpathentry kind="output" path="bin"/>
                    </classpath>'''
            }
        }
        root
    }

    private def createTestProxyFiles() {
        new File(dependencyTempFolder.root, 'gradle-bin.zip') << ''
        new File(dependencyTempFolder.root, 'not-a-real-dependency-0.0.pom') << '''<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>not-a-real-group</groupId>
  <artifactId>not-a-real-dependency</artifactId>
  <version>0.0</version>
  <packaging>jar</packaging>
</project>'''
    }

    private ILaunch createLaunchMock() {
        Mock(ILaunch)
    }

    private def createLaunchConfigurationMock(String path) {
        ILaunchConfiguration launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getAttribute('tasks', _) >> ['clean', 'dependencies']
        launchConfiguration.getAttribute('gradle_distribution', _) >> 'GRADLE_DISTRIBUTION(WRAPPER)'
        launchConfiguration.getAttribute('working_dir', _) >> path
        launchConfiguration.getAttribute('arguments', _) >> [
            '--refresh-dependencies',
            '-Dgradle.user.home=' + gradleHomeTempFolder.root
        ]
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }

    private def getProjectConfiguration(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(GradleDistribution.forRemoteDistribution("http://not.a.real.domain/gradlew/dist".toURI()))
        configuration.gradleUserHome = gradleHomeTempFolder.root
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        configuration
    }

}