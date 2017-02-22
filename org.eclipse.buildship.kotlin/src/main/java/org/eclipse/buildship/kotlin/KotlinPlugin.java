/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin;

import org.osgi.framework.BundleContext;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The activator class controls the plug-in life cycle.
 */
public class KotlinPlugin extends AbstractUIPlugin {
    public static final String PLUGIN_ID = "org.eclipse.buildship.kotlin"; //$NON-NLS-1$

    private static KotlinPlugin plugin;

    public KotlinPlugin() {
    }

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

    public static KotlinPlugin getInstance() {
        return plugin;
    }
}
