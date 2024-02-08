/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

/**
 * Helper class to activate plugins which contribute to the {@code executionparticipants} extension point.
 */
final class BuildExecutionParticipants {

    // the fully-qualified name of the extension point
    private static final String EXECUTION_PARTICIPANTS_EXTENSION_ID = CorePlugin.PLUGIN_ID + ".executionparticipants";

    // the attribute specifying the plugin id to start
    private static final String EXTENSION_ATTRIBUTE_PLUGIN_ID = "id";

    private BuildExecutionParticipants() {
    }

    public static void activateParticipantPlugins() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXECUTION_PARTICIPANTS_EXTENSION_ID);
        for (IConfigurationElement element : elements) {
            String pluginId = element.getAttribute(EXTENSION_ATTRIBUTE_PLUGIN_ID);
            try {
                // start the bundle in case it is not active yet
                // for details, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=478054
                Bundle bundle = Platform.getBundle(pluginId);
                if (Bundle.ACTIVE != bundle.getState()) {
                    bundle.start(Bundle.START_TRANSIENT);
                }
            } catch (BundleException e) {
                String message = String.format("Failed to activate plugin %s referenced in extension point 'executionparticipants'.", pluginId);
                CorePlugin.logger().error(message, e);
            }
        }
    }

}
