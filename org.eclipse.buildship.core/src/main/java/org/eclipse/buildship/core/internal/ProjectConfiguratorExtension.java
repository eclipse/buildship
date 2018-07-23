package org.eclipse.buildship.core.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.buildship.GradleProjectConfigurator;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import com.google.common.collect.Lists;

public final class ProjectConfiguratorExtension {

    public static List<GradleProjectConfigurator> loadConfigurators() {
        IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(CorePlugin.PLUGIN_ID, "projectconfigurators");
        ArrayList<GradleProjectConfigurator> result = Lists.newArrayList();
        for (int i = 0; i < elements.length; i++) {
            IConfigurationElement element = elements[i];
            try {
                result.add(GradleProjectConfigurator.class.cast(element.createExecutableExtension("class")));
            } catch (CoreException e) {
                CorePlugin.logger().warn("Can't load contributed invocation customizers", e);
            }
        }
        return result;
    }
}
