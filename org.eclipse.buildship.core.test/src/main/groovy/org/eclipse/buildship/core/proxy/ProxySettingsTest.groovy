/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat, Inc.) - Story: Integrate Eclipse proxy settings into Buildship model loading and task execution
 */

package org.eclipse.buildship.core.proxy

import com.google.common.util.concurrent.FutureCallback
import java.lang.management.ManagementFactory
import java.lang.management.RuntimeMXBean
import org.eclipse.core.runtime.jobs.Job

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.util.Pair
import org.eclipse.debug.core.ILaunch
import org.eclipse.core.net.proxy.IProxyService
import org.eclipse.core.internal.jobs.ThreadJob;
import org.eclipse.core.net.proxy.IProxyData
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob
import org.eclipse.buildship.core.util.progress.ToolingApiJob
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.buildship.core.launch.RunGradleConfigurationDelegateJob
import org.eclipse.buildship.core.launch.internal.DefaultGradleLaunchConfigurationManager
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.core.resources.ResourcesPlugin
//import org.eclipse.swt.widgets.Display

import org.junit.Rule
import org.junit.rules.TemporaryFolder
//import org.eclipse.ui.PlatformUI

import org.eclipse.buildship.core.proxy.support.*
import org.eclipse.buildship.core.test.fixtures.*

import org.eclipse.buildship.core.console.ProcessStreams
import org.eclipse.buildship.core.console.ProcessStreamsProvider
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import com.gradleware.tooling.toolingclient.BuildLaunchRequest
import com.gradleware.tooling.toolingclient.ToolingClient
import org.hamcrest.*
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification

class ProxySettingsTest extends ProjectImportSpecification {

    @Rule
    TemporaryFolder tempFolder
    ProcessStreamsProvider processStreamsProvider
    @Rule public final HttpServer server = new HttpServer()
    @Rule TestProxyServer proxyServer = new TestProxyServer(server)

    def proxyHost = 'localhost'
    def permHost = 'permHost'
    def tempHost = 'tempHost'
    def userId = 'test-user'
    def password = 'test-password'

    def setup() {
        server.start()
    }

    def "Eclipse proxy settings are properly collected"() {
        def proxyConfigurator = new EclipseProxySettingsSupporter()

        setup:
        setupTestProxyData(proxyHost, proxyServer.port)

        when:
        proxyConfigurator.configureEclipseProxySettings()

        then:
        System.getProperty("http.proxyHost") == proxyHost
        System.getProperty("http.proxyPort") == proxyServer.port.toString()
        System.getProperty("http.proxyUser") == userId
        System.getProperty("http.proxyPassword") == password

        //        System.getProperty("https.proxyHost") == "test-host-https"
        //        System.getProperty("https.proxyPort") == "8081"
        //        System.getProperty("https.proxyUser") == "test-id-https"
        //        System.getProperty("https.proxyPassword") == "test-password-https"
    }

    def "System properties temporarily changed when ToolingApiWorkspaceJob is run"() {
        String retrievedHost

        setup:
        setupTestProxyData(tempHost, 8080)
        System.setProperty("http.proxyHost", permHost)
        def job = new ToolingApiWorkspaceJob("Test") {
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
        String retrievedHost, abc

        setup:
        setupTestProxyData(tempHost, 0000)
        System.setProperty("http.proxyHost", permHost)
        def job = new ToolingApiJob("Test") {
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

    //    def "Different proxy settings can be used by subsequent builds"() {
    //        String permHost, secondTempHost
    //
    //        setup:
    //        permHost = "permHost"
    //        System.setProperty("http.proxyHost", permHost)
    //        setupTestProxyData()
    //
    //        def firstJob = new ToolingApiJob("Test") {
    //                    @Override
    //                    protected void runToolingApiJob(IProgressMonitor monitor) {
    //                    }
    //                }
    //
    //        def secondJob = new ToolingApiJob("Test") {
    //                    @Override
    //                    protected void runToolingApiJob(IProgressMonitor monitor) {
    //                        secondTempHost = System.getProperty("http.proxyHost")
    //                    }
    //                }
    //
    //        when:
    //        firstJob.schedule()
    //        firstJob.join()
    //        secondJob.schedule()
    //        secondJob.join()
    //
    //        then:
    //        secondTempHost == "test-host2"
    //        System.getProperty("http.proxyHost") == permHost
    //    }

//    def "Proxies are accessed upon Gradle distribution download"() {
//        setup:
//        proxyServer.start()
//        proxyServer.requireAuthentication(userId, password)
//        setupTestProxyData("localhost", proxyServer.port)
//        File rootProject = newProject(true, true)
//        server.expectGet("/gradlew/dist", new File("resources/gradle-distribution/gradle-2.6-bin.zip"))
//        Job job = newProjectImportJob(rootProject)
//
//        when:
//        job.schedule()
//        job.join()
//
//        then:
//        proxyServer.requestCount == 1
//    }

    def "Proxies are accessed upon task exectution"() {
        setup:
        println Thread.currentThread().getId()
        proxyServer.start()
//        proxyServer.requireAuthentication(userId, password)
        setupTestProxyData("localhost", proxyServer.port)
        File rootProject = newProject(false, true)
        server.expectHead("/fake-group/not-a-real-dependency/0.0/not-a-real-dependency-0.0.pom", new File("resources/proxy-test/not-a-real-dependency-0.0.pom"))
        server.expectGet ("/fake-group/not-a-real-dependency/0.0/not-a-real-dependency-0.0.pom", new File("resources/proxy-test/not-a-real-dependency-0.0.pom"))
        server.expectHead("/fake-group/not-a-real-dependency/0.0/not-a-real-dependency-0.0.jar", new File("resources/proxy-test/not-a-real-dependency-0.0.jar"))
        server.expectGet ("/fake-group/not-a-real-dependency/0.0/not-a-real-dependency-0.0.jar", new File("resources/proxy-test/not-a-real-dependency-0.0.jar"))
        server.expectHead("/fake-group/not-a-real-dependency/0.0/not-a-real-dependency-0.0.pom.sha1", new File("resources/proxy-test/not-a-real-dependency-0.0.pom.sha1"))
        server.expectGet ("/fake-group/not-a-real-dependency/0.0/not-a-real-dependency-0.0.pom.sha1", new File("resources/proxy-test/not-a-real-dependency-0.0.pom.sha1"))
        def job = new RunGradleConfigurationDelegateJob(createLaunchMock(), createLaunchConfigurationMock(rootProject.absolutePath))

        when:
        job.schedule()
        job.join()

        then:
        proxyServer.requestCount == 3
    }

    def setupTestProxyData(String host, int port) {
        IProxyService proxyService = CorePlugin.getProxyService()

        //Ian-todo: Check to make sure that these are necessary
        proxyService.setProxiesEnabled(true)
        proxyService.setSystemProxiesEnabled(true)

        IProxyData httpProxyData = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE)
        IProxyData httpsProxyData = proxyService.getProxyData(IProxyData.HTTPS_PROXY_TYPE)

        httpProxyData.setHost(host)
        httpProxyData.setPort(port)
        httpProxyData.setUserid(userId)
        httpProxyData.setPassword(password)

        // Ian-todo: Check to see if https needs to be tested, as well
        httpsProxyData.setHost(host + 'https')
        httpsProxyData.setPort(port)
        httpsProxyData.setUserid(userId)
        httpsProxyData.setPassword(password)

        proxyService.setProxyData((IProxyData[]) [
            httpProxyData,
            httpsProxyData
        ])
    }

    def newProjectImportJob(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(GradleDistribution.forRemoteDistribution("http://not.a.real.domain/gradlew/dist".toURI()))

        // Ian-todo: In PR, emphasize that gradleUserHome has been changed. Perhaps also test to see if gradle distribution exists in workspace.
        configuration.gradleUserHome = new File(ResourcesPlugin.getWorkspace().root.rawLocation.toString())
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        new org.eclipse.buildship.core.projectimport.ProjectImportJob(configuration, AsyncHandler.NO_OP)
    }

    // Ian-todo: Extend this to include remote dependencies
    def newProject(boolean projectDescriptorExists, boolean applyJavaPlugin) {
        def root = tempFolder.newFolder('simple-project')
        new File(root, 'build.gradle') << (applyJavaPlugin ? '''apply plugin: "java"
repositories { maven { url "http://not.a.real.domain" } }
dependencies { compile "fake-group:not-a-real-dependency:0.0" }''' : '')
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

    private ILaunch createLaunchMock() {
        Mock(ILaunch)
    }

    private def createLaunchConfigurationMock(String path) {
        ILaunchConfiguration launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getAttribute('tasks', _) >> ['clean', 'dependencies']
        launchConfiguration.getAttribute('gradle_distribution', _) >> 'GRADLE_DISTRIBUTION(WRAPPER)'
        launchConfiguration.getAttribute('working_dir', _) >> path
        launchConfiguration.getAttribute('arguments', _) >> ['--refresh-dependencies', '--info', '-S']
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }

}