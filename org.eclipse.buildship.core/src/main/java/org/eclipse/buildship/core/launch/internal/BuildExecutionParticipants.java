/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 478054
 */

package org.eclipse.buildship.core.launch.internal;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Helper class to activate plugins which contribute to the {@code executionparticipants} extension point.
 */
public final class BuildExecutionParticipants {

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
                Bundle bundle = Platform.getBundle(pluginId);

                // only start bundle in case it is not active yet
                if (Bundle.ACTIVE == bundle.getState()) {
                    return;
                }
                // start the specified plugin
                bundle.start(Bundle.START_TRANSIENT);
            } catch (Exception e) {
                String message = String.format("Failed to activate plugin %s referenced in extension point 'executionparticipants'.", pluginId);
                CorePlugin.logger().error(message, e);
            }
        }
    }

}
