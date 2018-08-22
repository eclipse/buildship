/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Updates the classpath provider attribute in the run configurations.
 * <p/>
 * To provide runtime classpath separation, Buildship replaces JDT's default classpath provider with
 * its own implementation. The injected {@code GradleClasspathProvider} instance filters entries
 * entries if Gradle provides the appropriate scope information.
 *
 * @see org.eclipse.buildship.core.internal.launch.impl.GradleClasspathProvider
 *
 * @author Donat Csikos
 */
public interface ExternalLaunchConfigurationManager {

    /**
     * Iterates through the existing run configurations and calls
     * {@link #updateClasspathProvider(ILaunchConfiguration)} if the configuration references the
     * target project.
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
     * non-Gradle project then Gradle classpath provider is restored to its original value.
     *
     * @param configuration the target configuration
     */
    void updateClasspathProvider(ILaunchConfiguration configuration);
}
