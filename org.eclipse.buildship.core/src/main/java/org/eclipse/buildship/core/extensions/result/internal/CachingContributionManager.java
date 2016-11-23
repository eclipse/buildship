/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.extensions.result.internal;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.extensions.InvocationCustomizer;
import org.eclipse.buildship.core.extensions.result.ContributionManager;

/**
 * Default implementation for {@link ContributionManager}. The contributed elements are loaded lazily
 * and cached.
 *
 * @author Donat Csikos
 */
public final class CachingContributionManager implements ContributionManager {

    private List<InvocationCustomizer> customizers;

    @Override
    public List<String> getContributedExtraArguments() {
        if (this.customizers == null) {
            this.customizers = loadCustomizers();
        }
        List<String> result = Lists.newArrayList();
        for (InvocationCustomizer customizer : this.customizers) {
            result.addAll(customizer.getExtraArguments());
        }
        return result;
    }

    private List<InvocationCustomizer> loadCustomizers() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(CorePlugin.PLUGIN_ID, "invocationcustomizers");
        ArrayList<InvocationCustomizer> result = Lists.newArrayList();
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            try {
                result.add(InvocationCustomizer.class.cast(element.createExecutableExtension("class")));
            } catch (CoreException e) {
                CorePlugin.logger().warn("Can't load contributed invocation customizers", e);
            }
        }
        return result;
    }
}
