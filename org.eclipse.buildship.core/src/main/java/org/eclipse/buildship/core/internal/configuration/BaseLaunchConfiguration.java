/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import java.io.File;
import java.util.List;

import org.eclipse.buildship.core.GradleDistribution;

/**
 * Contains properties all launch configurations share.
 */
public interface BaseLaunchConfiguration {

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
