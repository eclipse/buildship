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
import org.eclipse.core.runtime.jobs.Job;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.util.Pair
import org.eclipse.core.net.proxy.IProxyService
import org.eclipse.core.net.proxy.IProxyData
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob
import org.eclipse.buildship.core.util.progress.ToolingApiJob

import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification

class ProxySettingsTest extends ProjectImportSpecification {

    def "Eclipse proxy settings are properly collected"() {
        setup:
        setupTestProxyData()

        when:
        def proxyConfigurator = new EclipseProxySettingsConfigurator()
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

    def "System properties temporarily changed when ToolingApiWorkspaceJob is run"() {
        String permHost, tempHost

        setup:
        permHost = "permHost"
        System.setProperty("http.proxyHost", permHost)
        setupTestProxyData()
        def job = new ToolingApiWorkspaceJob("Test") {
                    @Override
                    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) {
                        tempHost = System.getProperty("http.proxyHost")
                    };
                }

        when:
        job.schedule()
        job.join();

        then:
        tempHost == "test-host"
        System.getProperty("http.proxyHost") == permHost
    }

    def "System properties temporarily changed when ToolingApiJob is run"() {
        String permHost, tempHost

        setup:
        permHost = "permHost"
        System.setProperty("http.proxyHost", permHost)
        setupTestProxyData()
        def job = new ToolingApiJob("Test") {
                    @Override
                    protected void runToolingApiJob(IProgressMonitor monitor) {
                        tempHost = System.getProperty("http.proxyHost")
                    };
                }

        when:
        job.schedule()
        job.join();

        then:
        tempHost == "test-host"
        System.getProperty("http.proxyHost") == permHost
    }

    def "JVM arguments are automatically set when Eclipse proxy settings are available"() {

    }

    def "Different proxy settings can be used by subsequent builds with different proxy settings"() {

    }

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

        proxyService.setProxyData((IProxyData[]) [httpProxyData, httpsProxyData])
    }
}
