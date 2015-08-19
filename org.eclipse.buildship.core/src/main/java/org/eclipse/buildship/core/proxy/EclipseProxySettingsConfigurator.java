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

package org.eclipse.buildship.core.proxy;

import com.google.common.base.Optional;

import org.eclipse.core.net.proxy.IProxyData;

import org.eclipse.buildship.core.CorePlugin;

public class EclipseProxySettingsConfigurator {

    String currentHTTPProxyHost, currentHTTPProxyPort, currentHTTPProxyUser, currentHTTPProxyPassword;
    String currentHTTPSProxyHost, currentHTTPSProxyPort, currentHTTPSProxyUser, currentHTTPSProxyPassword;

    public void configureEclipseProxySettings() {
        configureHTTPProxySettings();
        configureHTTPSProxySettings();
    }

    public void storeSystemProxySettings() {
        this.currentHTTPProxyHost = System.getProperty("http.proxyHost");
        this.currentHTTPProxyPort = System.getProperty("http.proxyPort");
        this.currentHTTPProxyUser = System.getProperty("http.proxyUser");
        this.currentHTTPProxyPassword = System.getProperty("http.proxyPassword");

        this.currentHTTPSProxyHost = System.getProperty("https.proxyHost");
        this.currentHTTPSProxyPort = System.getProperty("https.proxyPort");
        this.currentHTTPSProxyUser = System.getProperty("https.proxyUser");
        this.currentHTTPSProxyPassword = System.getProperty("https.proxyPassword");
    }


    public void restoreSystemProxySettings() {
        System.setProperty("http.proxyHost", this.currentHTTPProxyHost);
        System.setProperty("http.proxyPort", this.currentHTTPProxyPort);
        System.setProperty("http.proxyUser", this.currentHTTPProxyUser);
        System.setProperty("http.proxyPassword", this.currentHTTPProxyPassword);

        System.setProperty("https.proxyHost", this.currentHTTPSProxyHost);
        System.setProperty("https.proxyPort", this.currentHTTPSProxyPort);
        System.setProperty("https.proxyUser", this.currentHTTPSProxyUser);
        System.setProperty("https.proxyPassword", this.currentHTTPSProxyPassword);
    }

    private void configureHTTPProxySettings() {
        Optional<IProxyData> httpProxyData = Optional.of(CorePlugin.getProxyService().getProxyData(IProxyData.HTTP_PROXY_TYPE));
        if (httpProxyData.isPresent()) {
            if (httpProxyData.get().getHost() != null) {
                System.setProperty("http.proxyHost", httpProxyData.get().getHost());
            }
            if (httpProxyData.get().getPort() != -1) {
                System.setProperty("http.proxyPort", Integer.toString(httpProxyData.get().getPort()));
            }
            if (httpProxyData.get().getUserId() != null) {
                System.setProperty("http.proxyUser", httpProxyData.get().getUserId());
            }
            if (httpProxyData.get().getPassword() != null) {
                System.setProperty("http.proxyPassword", httpProxyData.get().getPassword());
            }
//            System.setProperty("http.nonProxyHosts", CorePlugin.getProxyService().getNonProxiedHosts());
        }
    }

    private void configureHTTPSProxySettings() {
        Optional<IProxyData> httpsProxyData = Optional.of(CorePlugin.getProxyService().getProxyData(IProxyData.HTTPS_PROXY_TYPE));
        if (httpsProxyData.isPresent()) {
            if (httpsProxyData.get().getHost() != null) {
                System.setProperty("https.proxyHost", httpsProxyData.get().getHost());
            }
            if (httpsProxyData.get().getPort() != -1) {
                System.setProperty("https.proxyPort", Integer.toString(httpsProxyData.get().getPort()));
            }
            if (httpsProxyData.get().getUserId() != null) {
                System.setProperty("https.proxyUser", httpsProxyData.get().getUserId());
            }
            if (httpsProxyData.get().getPassword() != null) {
                System.setProperty("https.proxyPassword", httpsProxyData.get().getPassword());
            }
//            System.setProperty("http.nonProxyHosts", CorePlugin.getProxyService().getNonProxiedHosts());
        }
    }

}
