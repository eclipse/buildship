/*******************************************************************************
 * Copyright (c) 2022 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.model;

import java.io.Serializable;
import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProject;

/**
 * Custom root model allowing Buildship to extract any information from a Gradle build apart from the built-in EclipseProject model.
 *
 * @author donat
 */
public interface ExtendedEclipseModel extends Serializable {

    /**
     * @return Structural information about the project in the build.
     */
    List<? extends ProjectInGradleConfiguration> getProjects();

    /**
     *
     * @return the EclipseProject model built into Gradle.
     */
    EclipseProject getEclipseProject();
}
