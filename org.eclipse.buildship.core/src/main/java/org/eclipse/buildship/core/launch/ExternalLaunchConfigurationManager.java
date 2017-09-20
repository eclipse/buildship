/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Manages the classpath provider attribute in the target run configurations.
 * <p/>
 * Buildship uses this manager to replace the default classpath provider used by JDT with its own
 * implementation. The new provider alters the runtime classpath based on the Gradle configuration
 *
 * @see org.eclipse.buildship.core.launch.internal.GradleClasspathProvider
 *
 * @author Donat Csikos
 */
public interface ExternalLaunchConfigurationManager {

    /**
     * Iterates through the existing run configurations and calls
     * {@link #updateClasspathProvider(ILaunchConfiguration)} on the ones that refer to the target
     * project.
     *
     * @param project the target project
     */
    void updateClasspathProviders(IProject project);

    /**
     * Updates the classpath provider on the target configuration.
     * <p/>
     * If the configuration type is not supported (e.g. not a JDT launch type) or the target project
     * can't be determined then the configuration is left untouched. If the configuration refers to
     * a Gradle project then the classpath provider is updated. If the configuration refers to a
     * non-Gradle project, then Gradle classpath provider is restored to its original value.
     *
     *
     * @param configuration the target configuration
     */
    void updateClasspathProvider(ILaunchConfiguration configuration);
}
