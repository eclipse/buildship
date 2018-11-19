/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.invocation.InvocationCustomizer;

public class DefaultExtensionManager implements ExtensionManager {

    @Override
    public List<InvocationCustomizer> loadCustomizers() {
        Collection<IConfigurationElement> elements = loadElements("invocationcustomizers");
        List<InvocationCustomizer> result = new ArrayList<>(elements.size());
        for (IConfigurationElement element : elements) {
            try {
                result.add(InvocationCustomizer.class.cast(element.createExecutableExtension("class")));
            } catch (Exception e) {
                CorePlugin.logger().warn("Cannot load invocationcustomizers extension", e);
            }
        }
        return result;
    }

    // TODO (donat) adjust documentation for new elements in the extension point
    // TODO (donat) restrict accessible models to the ones referenced in the extension declaration

    @Override
    public List<ProjectConfiguratorContribution> loadConfigurators() {
        Collection<IConfigurationElement> elements = loadElements("projectconfigurators");
        List<ProjectConfiguratorContribution> result = new ArrayList<>(elements.size());
        for (IConfigurationElement element : elements) {
            if ("configurator".equals(element.getName())) {
                result.add(ProjectConfiguratorContribution.from(element));
            }
        }
        return result;
    }

    @Override
    public List<BuildActionContribution> loadBuildActions() {
        Collection<IConfigurationElement> elements = loadElements("projectconfigurators");
        List<BuildActionContribution> result = new ArrayList<>(elements.size());
        for (IConfigurationElement element : elements) {
            if ("buildAction".equals(element.getName())) {
                BuildActionContribution contribution = BuildActionContribution.from(element);
                result.add(contribution);
            }
        }
        return result;
    }

    @VisibleForTesting
    Collection<IConfigurationElement> loadElements(String extensionPointName) {
        return Arrays.asList(Platform.getExtensionRegistry().getConfigurationElementsFor(CorePlugin.PLUGIN_ID, extensionPointName));
    }
}
