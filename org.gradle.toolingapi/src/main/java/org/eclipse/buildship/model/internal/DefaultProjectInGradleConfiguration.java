/*******************************************************************************
 * Copyright (c) 2022 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.model.internal;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.eclipse.buildship.model.ProjectInGradleConfiguration;


public class DefaultProjectInGradleConfiguration implements ProjectInGradleConfiguration, Serializable {

    private final File location;
    private final List<String> sourceSetNames;

    public DefaultProjectInGradleConfiguration(File location, List<String> sourceSetNames) {
        this.location = location;
        this.sourceSetNames = sourceSetNames;
    }

    @Override
    public File getLocation() {
        return this.location;
    }

    @Override
    public List<String> getSourceSetNames() {
        return this.sourceSetNames;
    }
}
