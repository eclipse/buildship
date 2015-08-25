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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import org.eclipse.core.runtime.jobs.Job;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.util.Pair
import org.eclipse.debug.core.ILaunch
import org.eclipse.core.net.proxy.IProxyService
import org.eclipse.core.net.proxy.IProxyData
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob
import org.eclipse.buildship.core.util.progress.ToolingApiJob
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.buildship.core.launch.RunGradleConfigurationDelegateJob;
import org.eclipse.buildship.core.launch.internal.DefaultGradleLaunchConfigurationManager;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import org.eclipse.buildship.core.proxy.support.*
import org.eclipse.buildship.core.test.fixtures.*

import org.eclipse.buildship.core.console.ProcessStreams
import org.eclipse.buildship.core.console.ProcessStreamsProvider
import org.eclipse.buildship.core.test.fixtures.TestEnvironment
import com.gradleware.tooling.toolingclient.BuildLaunchRequest
import com.gradleware.tooling.toolingclient.ToolingClient
import org.hamcrest.*;

import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification

class ProxySettingsTest extends ProjectImportSpecification {

    @Rule
    TemporaryFolder tempFolder
    ToolingClient toolingClient
    ProcessStreamsProvider processStreamsProvider
    @Rule public final HttpServer server = new HttpServer()
    @Rule TestProxyServer proxyServer = new TestProxyServer(server)

    def setup() {
        BuildLaunchRequest request = Mock(BuildLaunchRequest)
        toolingClient = Mock(ToolingClient)
        toolingClient.newBuildLaunchRequest(_) >> request

        OutputStream configurationStream = Mock(OutputStream)
        ProcessStreams processStreams = Mock(ProcessStreams)
        processStreams.getConfiguration() >> configurationStream

        processStreamsProvider = Mock(ProcessStreamsProvider)
        processStreamsProvider.createProcessStreams(_) >> processStreams
        processStreamsProvider.getBackgroundJobProcessStreams() >> processStreams

        TestEnvironment.registerService(ToolingClient, toolingClient)
        TestEnvironment.registerService(ProcessStreamsProvider, processStreamsProvider)

        // From AbstractHttpDependencyResolutionTest
//        server.expectUserAgent(matchesNameAndVersion("Gradle", GradleVersion.current().getVersion()))
        server.start()
        proxyServer.start()
    }

    def cleanup() {
        TestEnvironment.cleanup()
    }

    def "Eclipse proxy settings are properly collected"() {
        setup:
        setupTestProxyData()

        when:
        def proxyConfigurator = new EclipseProxySettingsSupporter()
        proxyConfigurator.configureEclipseProxySettings()

        then:
        System.getProperty("http.proxyHost") == "test-host"
        System.getProperty("http.proxyPort") == "8080"
        System.getProperty("http.proxyUser") == "test-id"
        System.getProperty("http.proxyPassword") == "test-password"

        System.getProperty("https.proxyHost") == "test-host-https"
        System.getProperty("https.proxyPort") == "8081"
        System.getProperty("https.proxyUser") == "test-id-https"
        System.getProperty("https.proxyPassword") == "test-password-https"
    }
    //
    //    def "System properties temporarily changed when ToolingApiWorkspaceJob is run"() {
    //        String permHost, tempHost
    //
    //        setup:
    //        permHost = "permHost"
    //        System.setProperty("http.proxyHost", permHost)
    //        setupTestProxyData()
    //        def job = new ToolingApiWorkspaceJob("Test") {
    //                    @Override
    //                    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
    //                        tempHost = System.getProperty("http.proxyHost")
    //                    };
    //                }
    //
    //        when:
    //        job.schedule()
    //        job.join();
    //
    //        then:
    //        tempHost == "test-host"
    //        System.getProperty("http.proxyHost") == permHost
    //    }
    //
    //    def "System properties temporarily changed when ToolingApiJob is run"() {
    //        String permHost, tempHost
    //
    //        setup:
    //        permHost = "permHost"
    //        System.setProperty("http.proxyHost", permHost)
    //        setupTestProxyData()
    //        def job = new ToolingApiJob("Test") {
    //                    @Override
    //                    protected void runToolingApiJob(IProgressMonitor monitor) {
    //                        tempHost = System.getProperty("http.proxyHost")
    //                    };
    //                }
    //
    //        when:
    //        job.schedule()
    //        job.join();
    //
    //        then:
    //        tempHost == "test-host"
    //        System.getProperty("http.proxyHost") == permHost
    //    }

    def "JVM arguments are automatically set when Eclipse proxy settings are available"() {
        setup:

        System.out.flush()
        setupTestProxyData()
        List<String> arguments;
        //        RunGradleConfigurationDelegateJob job = new RunGradleConfigurationDelegateJob(createLaunchMock(), createLaunchConfigurationMock())
        def job = new ToolingApiJob("Test") {
                    @Override
                    protected void runToolingApiJob(IProgressMonitor monitor) {
                        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
                        arguments = runtimeMxBean.getInputArguments();
                    };
                }
        when:
        job.schedule()
        job.join()

        then:
        System.out.println(">> : " + arguments)
    }

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
    //                    };
    //                }
    //
    //        def secondJob = new ToolingApiJob("Test") {
    //                    @Override
    //                    protected void runToolingApiJob(IProgressMonitor monitor) {
    //                        secondTempHost = System.getProperty("http.proxyHost")
    //                    };
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
    //

    def "Proxies are accessed upon task exectution"() {

    }

    def setupTestProxyData() {
        IProxyService proxyService = CorePlugin.getProxyService()
        IProxyData httpProxyData = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE)
        IProxyData httpsProxyData = proxyService.getProxyData(IProxyData.HTTPS_PROXY_TYPE)

        httpProxyData.setHost("test-host")
        httpProxyData.setPort(8080)
        httpProxyData.setUserid("test-id")
        httpProxyData.setPassword("test-password")

        httpsProxyData.setHost("test-host-https")
        httpsProxyData.setPort(8081)
        httpsProxyData.setUserid("test-id-https")
        httpsProxyData.setPassword("test-password-https")

        proxyService.setProxyData((IProxyData[]) [
            httpProxyData,
            httpsProxyData
        ])
    }

    def setupSecondTestProxyData() {
        IProxyService proxyService = CorePlugin.getProxyService()
        IProxyData httpProxyData = proxyService.getProxyData(IProxyData.HTTP_PROXY_TYPE)
        IProxyData httpsProxyData = proxyService.getProxyData(IProxyData.HTTPS_PROXY_TYPE)

        httpProxyData.setHost("test-host2")
        httpProxyData.setPort(8080)
        httpProxyData.setUserid("test-id")
        httpProxyData.setPassword("test-password")

        httpsProxyData.setHost("test-host-https")
        httpsProxyData.setPort(8081)
        httpsProxyData.setUserid("test-id-https")
        httpsProxyData.setPassword("test-password-https")

        proxyService.setProxyData((IProxyData[]) [
            httpProxyData,
            httpsProxyData
        ])
    }


    private ILaunch createLaunchMock() {
        Mock(ILaunch)
    }

    private def createLaunchConfigurationMock() {
        def launchConfiguration = Mock(ILaunchConfiguration)
        launchConfiguration.getAttribute('tasks', _) >> ['clean', 'build']
        launchConfiguration.getAttribute('gradle_distribution', _) >> 'GRADLE_DISTRIBUTION(WRAPPER)'
        launchConfiguration.getAttribute('working_dir', _) >> tempFolder.newFolder().absolutePath
        launchConfiguration.getAttribute('arguments', _) >> []
        launchConfiguration.getAttribute('jvm_arguments', _) >> []
        launchConfiguration
    }


    public static Matcher matchesNameAndVersion(String applicationName, String version) {
        return new UserAgentMatcher(applicationName, version);
    }

}
