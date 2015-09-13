package org.eclipse.buildship.core.launch.internal;

import org.eclipse.buildship.core.launch.LaunchConfigurationTools;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Empty implementation of the {@code LaunchConfigurationTools} interface.
 */
public final class EmptyLaunchConfigurationTools implements LaunchConfigurationTools {

    @Override
    public void launch(ILaunchConfiguration configuration, String mode) {
        throw new UnsupportedOperationException("The Core Plugin does not support launching launch configurations.");
    }

}
