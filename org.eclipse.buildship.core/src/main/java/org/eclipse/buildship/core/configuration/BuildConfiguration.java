/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration;

import java.io.File;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

/**
 * Configuration for for a Gradle project in the workspace.
 *
 * @author Donat Csikos
 */
public interface BuildConfiguration {

    WorkspaceConfiguration getWorkspaceConfiguration();

    File getRootProjectDirectory();

    boolean isOverrideWorkspaceSettings();

    File getGradleUserHome();

    GradleDistribution getGradleDistribution();

    boolean isBuildScansEnabled();

    boolean isOfflineMode();

    FixedRequestAttributes toRequestAttributes();
}
