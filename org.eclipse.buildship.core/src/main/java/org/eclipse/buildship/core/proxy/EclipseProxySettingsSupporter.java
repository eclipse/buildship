/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat, Inc.) - Bug 471943 - Make Buildship work behind the firewall
 */

package org.eclipse.buildship.core.proxy;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.common.base.Optional;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.buildship.core.CorePlugin;

/**
 * This class propagates Eclipse's proxy settings to System settings, and ensures that the existing System proxy settings
 * are persisted.
 */
public class EclipseProxySettingsSupporter {

    private static String savedHTTPProxyHost, savedHTTPProxyPort, savedHTTPProxyUser, savedHTTPProxyPassword;
    private static final Lock lock = new ReentrantLock();

    /**
     * Configures the System properties proxy settings based on the Eclipse proxy settings.
     */
    public static void configureEclipseProxySettings() {
        synchronized(EclipseProxySettingsSupporter.class) {
            storeSystemProxySettings();
            configureHTTPProxySettings();
        }
    }

    private static void configureHTTPProxySettings() {
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

    /**
     * Must be called in conjunction with the {@link #restoreSystemProxySettings() restoreSystemProxySettings}
     * restoreSystemProxySettings method.
     */
    private static void storeSystemProxySettings() {
        // The thread that owns the lock is the thread that is responsible for ensuring that the System
        // proxy settings persist. If a thread does not/cannot hold the lock, it does not have the responsibility.
        if (EclipseProxySettingsSupporter.lock.tryLock()) {
            EclipseProxySettingsSupporter.savedHTTPProxyHost = System.getProperty("http.proxyHost");
            EclipseProxySettingsSupporter.savedHTTPProxyPort = System.getProperty("http.proxyPort");
            EclipseProxySettingsSupporter.savedHTTPProxyUser = System.getProperty("http.proxyUser");
            EclipseProxySettingsSupporter.savedHTTPProxyPassword = System.getProperty("http.proxyPassword");
        }
    }

    /**
     * Restores the proxy settings in the system properties to what they were before
     * the EclipseProxySettingsSupporter changed them.
     */
    public static void restoreSystemProxySettings() {
        if (((ReentrantLock) EclipseProxySettingsSupporter.lock).isHeldByCurrentThread()) {
            resetOrClearSystemProperty("http.proxyHost", EclipseProxySettingsSupporter.savedHTTPProxyHost);
            resetOrClearSystemProperty("http.proxyPort", EclipseProxySettingsSupporter.savedHTTPProxyPort);
            resetOrClearSystemProperty("http.proxyUser", EclipseProxySettingsSupporter.savedHTTPProxyUser);
            resetOrClearSystemProperty("http.proxyPassword", EclipseProxySettingsSupporter.savedHTTPProxyPassword);
            EclipseProxySettingsSupporter.lock.unlock();
        }
    }

    private static void resetOrClearSystemProperty(String property, String savedValue) {
        if (savedValue == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, savedValue);
        }
    }

}
