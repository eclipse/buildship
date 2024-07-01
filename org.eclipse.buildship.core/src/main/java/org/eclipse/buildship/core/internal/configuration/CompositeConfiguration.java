/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.core.internal.configuration;

import java.io.File;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Configuration for a workspace composite in a Gradle build.
 *
 * @author Sebastian Kuzniarz
 */
public interface CompositeConfiguration {

    File getCompositeDir();

    IAdaptable[] getProjectList();

    BuildConfiguration getBuildConfiguration();

    Boolean projectAsCompositeRoot();

    File getRootProject();
}
