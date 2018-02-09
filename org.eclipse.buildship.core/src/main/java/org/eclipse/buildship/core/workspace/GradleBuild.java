/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

import java.io.Writer;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.TestLauncher;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.configuration.RunConfiguration;
import org.eclipse.buildship.core.util.gradle.TransientRequestAttributes;

/**
 * A Gradle build.
 *
 * @author Stefan Oehme
 */
public interface GradleBuild {

    /**
     * Attempts to synchronize the build with the workspace.
     * <p/>
     * The synchronization happens synchronously. In case of a failure, the method throws a
     * {@link CoreException} which contains the necessary status and error message about the
     * failure.
     *
     * @param newProjectHandler how to handle newly added projects
     * @param tokenSource the cancellation token source
     * @throws CoreException if the synchronization fails
     * @see org.eclipse.buildship.core.operation.ToolingApiStatus
     */
    void synchronize(NewProjectHandler newProjectHandler, CancellationTokenSource tokenSource, IProgressMonitor monitor) throws CoreException;

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
     * @param configWriter the writer to which the build launcher configuration should be printed
     * @param transientAttributes the transient attributes for the launcher.
     * @return the build launcher
     */
    BuildLauncher newBuildLauncher(RunConfiguration runConfiguration, Writer configWriter, TransientRequestAttributes transientAttributes);

    /**
     * Creates a new Gradle test launcher. The method automatically opens a new Tooling API
     * connection which is closed after the {@code run()} method is finished.
     *
     * @param runConfiguration the run configuration to configure the connection with
     * @param configWriter the writer to which the build launcher configuration should be printed
     * @param transientAttributes the transient attributes for the launcher.
     * @return the test launcher
     */
    TestLauncher newTestLauncher(RunConfiguration runConfiguration, Writer configWriter, TransientRequestAttributes transientAttributes);

    /**
     * Returns build config used for this build.
     *
     * @return the build config, never null
     */
    BuildConfiguration getBuildConfig();
}
