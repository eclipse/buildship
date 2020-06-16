/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
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

/**
 * Configuration for a workspace composite in a Gradle build.
 *
 * @author Sebastian Kuzniarz
 */
public interface CompositeConfiguration {

    String getCompositeName();

    List<File> getIncludedBuilds();

    BuildConfiguration getBuildConfiguration();

    Boolean projectAsCompositeRoot();
}
