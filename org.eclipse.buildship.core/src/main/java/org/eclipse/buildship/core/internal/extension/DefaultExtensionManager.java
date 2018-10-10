/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.extension;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.invocation.InvocationCustomizer;

public final class DefaultExtensionManager implements ExtensionManager {

    @Override
    public List<InvocationCustomizer> loadCustomizers() {
        return loadExtensions("invocationcustomizers", InvocationCustomizer.class);
    }

    @Override
    public List<ProjectConfigurator> loadConfigurators() {
        return loadExtensions("projectconfigurators", ProjectConfigurator.class);
    }

    private static <T> List<T> loadExtensions(String extensionPointName, Class<T> cls) {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(CorePlugin.PLUGIN_ID, extensionPointName);
        ArrayList<T> result = Lists.newArrayList();
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            try {
                result.add(cls.cast(element.createExecutableExtension("class")));
            } catch (CoreException e) {
                CorePlugin.logger().warn("Cannot load " + extensionPointName + " extension" , e);
            }
        }
        return result;
    }
}
