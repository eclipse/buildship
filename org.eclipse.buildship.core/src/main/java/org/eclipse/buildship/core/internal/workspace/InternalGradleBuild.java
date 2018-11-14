/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.internal.workspace;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.TestLauncher;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;

/**
 * A Gradle build.
 *
 * @author Stefan Oehme
 */
public interface InternalGradleBuild extends GradleBuild {

    /**
     * Returns the model provider for this build.
     *
     * @return the model provider, never null
     */
    ModelProvider getModelProvider();

    /**
     * Creates a new Gradle build launcher. The method automatically opens a new Tooling API
     * connection which is closed after the {@code run()} method is finished.
     *
     * @param runConfiguration the run configuration to configure the connection with
     * @param progressAttributes the progress attributes for the launcher.
     * @return the build launcher
     */
    BuildLauncher newBuildLauncher(RunConfiguration runConfiguration, GradleProgressAttributes progressAttributes);

    /**
     * Creates a new Gradle test launcher. The method automatically opens a new Tooling API
     * connection which is closed after the {@code run()} method is finished.
     *
     * @param runConfiguration the run configuration to configure the connection with
     * @param progressAttributes the progress attributes for the launcher.
     * @return the test launcher
     */
    TestLauncher newTestLauncher(RunConfiguration runConfiguration, GradleProgressAttributes progressAttributes);

    /**
     * Returns build config used for this build.
     *
     * @return the build config, never null
     */
    BuildConfiguration getBuildConfig();
}
