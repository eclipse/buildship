/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.util.function.Function;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.internal.configuration.BuildConfiguration;
import org.eclipse.buildship.core.internal.configuration.RunConfiguration;
import org.eclipse.buildship.core.internal.configuration.TestRunConfiguration;
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
    TestLauncher newTestLauncher(TestRunConfiguration runConfiguration, GradleProgressAttributes progressAttributes);

    /**
     * Returns build config used for this build.
     *
     * @return the build config, never null
     */
    BuildConfiguration getBuildConfig();


    /**
     * Executes an action in the Gradle runtime.
     *
     * @see #withConnection(Function, IProgressMonitor)
     */
    <T> T withConnection(Function<ProjectConnection, ? extends T> action, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws Exception;
}
