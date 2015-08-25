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

public class EclipseProxySettingsSupporter {

    String savedHTTPProxyHost, savedHTTPProxyPort, savedHTTPProxyUser, savedHTTPProxyPassword;
    String savedHTTPSProxyHost, savedHTTPSProxyPort, savedHTTPSProxyUser, savedHTTPSProxyPassword;

    public void configureEclipseProxySettings() {
        configureHTTPProxySettings();
        configureHTTPSProxySettings();
    }

    public void storeSystemProxySettings() {
        this.savedHTTPProxyHost = System.getProperty("http.proxyHost");
        this.savedHTTPProxyPort = System.getProperty("http.proxyPort");
        this.savedHTTPProxyUser = System.getProperty("http.proxyUser");
        this.savedHTTPProxyPassword = System.getProperty("http.proxyPassword");

        this.savedHTTPSProxyHost = System.getProperty("https.proxyHost");
        this.savedHTTPSProxyPort = System.getProperty("https.proxyPort");
        this.savedHTTPSProxyUser = System.getProperty("https.proxyUser");
        this.savedHTTPSProxyPassword = System.getProperty("https.proxyPassword");
    }


    public void restoreSystemProxySettings() {
        resetOrClearSystemProperty("http.proxyHost", this.savedHTTPProxyHost);
        resetOrClearSystemProperty("http.proxyPort", this.savedHTTPProxyPort);
        resetOrClearSystemProperty("http.proxyUser", this.savedHTTPProxyUser);
        resetOrClearSystemProperty("http.proxyPassword", this.savedHTTPProxyPassword);

        resetOrClearSystemProperty("https.proxyHost", this.savedHTTPSProxyHost);
        resetOrClearSystemProperty("https.proxyPort", this.savedHTTPSProxyPort);
        resetOrClearSystemProperty("https.proxyUser", this.savedHTTPSProxyUser);
        resetOrClearSystemProperty("https.proxyPassword", this.savedHTTPSProxyPassword);
    }

    private void configureHTTPProxySettings() {
        Optional<IProxyData> httpProxyData = Optional.of(CorePlugin.getProxyService().getProxyData(IProxyData.HTTP_PROXY_TYPE));
        if (httpProxyData.isPresent()) {
            if (httpProxyData.get().getHost() != null) {
                System.setProperty("http.proxyHost", httpProxyData.get().getHost());
            } else {
                System.setProperty("http.proxyHost", "value");
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
            // System.setProperty("http.nonProxyHosts", CorePlugin.getProxyService().getNonProxiedHosts());
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
            // System.setProperty("http.nonProxyHosts", CorePlugin.getProxyService().getNonProxiedHosts());
        }
    }

    private void resetOrClearSystemProperty(String property, String savedValue) {
        if (savedValue == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, savedValue);
        }
    }

}
