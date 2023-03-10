/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import org.eclipse.buildship.core.GradleDistribution;

/**
 * Contains properties all run configurations share.
 */
public interface BaseRunConfiguration {

    ProjectConfiguration getProjectConfiguration();

    GradleDistribution getGradleDistribution();

    File getGradleUserHome();

    File getJavaHome();

    List<String> getArguments();

    List<String> getJvmArguments();

    boolean isShowExecutionView();

    boolean isShowConsoleView();

    GradleArguments toGradleArguments();
}
