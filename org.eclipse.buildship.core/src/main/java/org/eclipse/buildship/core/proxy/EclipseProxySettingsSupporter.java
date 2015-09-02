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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Optional;

import org.eclipse.core.net.proxy.IProxyData;

import org.eclipse.buildship.core.CorePlugin;

public class EclipseProxySettingsSupporter {

    private String savedHTTPProxyHost, savedHTTPProxyPort, savedHTTPProxyUser, savedHTTPProxyPassword;
    private static final Lock lock = new ReentrantLock();

    /**
     * Configures the System properties proxy settings based on the Eclipse proxy settings.
     */
    public void configureEclipseProxySettings() {
        storeSystemProperties();
        configureHTTPProxySettings();
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
        }
    }

    private void storeSystemProperties() {
        if (EclipseProxySettingsSupporter.lock.tryLock()) {
            this.savedHTTPProxyHost = System.getProperty("http.proxyHost");
            this.savedHTTPProxyPort = System.getProperty("http.proxyPort");
            this.savedHTTPProxyUser = System.getProperty("http.proxyUser");
            this.savedHTTPProxyPassword = System.getProperty("http.proxyPassword");
        }
    }

    /**
     * Restores the proxy settings in the system properties to what they were before
     * the EclipseProxySettingsSupporter changed them.
     */
    public void restoreSystemProxySettings() {
        resetOrClearSystemProperty("http.proxyHost", this.savedHTTPProxyHost);
        resetOrClearSystemProperty("http.proxyPort", this.savedHTTPProxyPort);
        resetOrClearSystemProperty("http.proxyUser", this.savedHTTPProxyUser);
        resetOrClearSystemProperty("http.proxyPassword", this.savedHTTPProxyPassword);
        EclipseProxySettingsSupporter.lock.unlock();
    }

    private void resetOrClearSystemProperty(String property, String savedValue) {
        if (savedValue == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, savedValue);
        }
    }

}
