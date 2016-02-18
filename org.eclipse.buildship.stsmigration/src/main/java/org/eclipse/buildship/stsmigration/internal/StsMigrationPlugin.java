/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.stsmigration.internal;

import java.lang.reflect.Field;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Represents the STS migration plugin.
 */
public final class StsMigrationPlugin extends AbstractUIPlugin implements StsMigrationState {

    public static final String PLUGIN_ID = "org.eclipse.buildship.stsmigration";

    private static final String STS_PLUGIN_ID = "org.springsource.ide.eclipse.gradle.core";
    private static final String NOTIFICATION_MUTED_PROPERTY = "notification.muted";

    private static StsMigrationPlugin plugin;

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    static StsMigrationPlugin getInstance() {
        return plugin;
    }

    static StsMigrationState getStsMigrationState() {
        return plugin;
    }

    @Override
    public boolean isStsPluginInstalled() {
        BundleContext bundleContext = getInstance().getBundle().getBundleContext();
        for (Bundle bundle : bundleContext.getBundles()) {
            if (STS_PLUGIN_ID.equals(bundle.getSymbolicName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isNotificationMuted() {
        return getConfigurationScope().getNode(StsMigrationPlugin.PLUGIN_ID).getBoolean(NOTIFICATION_MUTED_PROPERTY, false);
    }

    @Override
    public void setNotificationMuted(boolean muted) {
        IEclipsePreferences node = getConfigurationScope().getNode(StsMigrationPlugin.PLUGIN_ID);
        node.putBoolean(NOTIFICATION_MUTED_PROPERTY, muted);
        try {
            node.flush();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }

    private static ConfigurationScope getConfigurationScope() {
        // in older Eclipse versions the {@code ConfigurationScope.INSTANCE} field does not exist
        try {
            Field field = ConfigurationScope.class.getField("INSTANCE");
            return (ConfigurationScope) field.get(null);
        } catch (Exception e1) {
            try {
                return ConfigurationScope.class.newInstance();
            } catch (Exception e2) {
                throw new RuntimeException(e2);
            }
        }
    }

}
