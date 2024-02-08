/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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

    @Override
    public List<ProjectConfiguratorContribution> loadConfigurators() {
        Collection<IConfigurationElement> elements = loadElements("projectconfigurators");
        List<ProjectConfiguratorContribution> result = new ArrayList<>(elements.size());
        for (IConfigurationElement element : elements) {
            ProjectConfiguratorContribution contribution = ProjectConfiguratorContribution.from(element);
            result.add(contribution);
        }
        return result;
    }

    @VisibleForTesting
    Collection<IConfigurationElement> loadElements(String extensionPointName) {
        return Arrays.asList(Platform.getExtensionRegistry().getConfigurationElementsFor(CorePlugin.PLUGIN_ID, extensionPointName));
    }
}
