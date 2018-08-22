/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.extension;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.invocation.InvocationCustomizer;

/**
 * Retrieves and caches extra Gradle arguments contributed via the {@code invocationcustomizer}
 * extension point.
 *
 * @author Donat Csikos
 */
public final class InvocationCustomizerCollector implements InvocationCustomizer {

    private List<InvocationCustomizer> customizers;

    @Override
    public List<String> getExtraArguments() {
        if (this.customizers == null) {
            this.customizers = loadCustomizers();
        }
        return collectArguments(this.customizers);
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

    private static List<String> collectArguments(List<InvocationCustomizer> customizers) {
        List<String> result = Lists.newArrayList();
        for (InvocationCustomizer customizer : customizers) {
            result.addAll(customizer.getExtraArguments());
        }
        return result;
    }
}
