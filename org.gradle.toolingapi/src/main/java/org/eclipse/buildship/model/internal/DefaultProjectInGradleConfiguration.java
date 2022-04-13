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
import java.util.Set;

import org.eclipse.buildship.model.ProjectInGradleConfiguration;
import org.eclipse.buildship.model.SourceSet;
import org.eclipse.buildship.model.TestTask;


public class DefaultProjectInGradleConfiguration implements ProjectInGradleConfiguration, Serializable {

    private final File location;
    private final Set<SourceSet> sourceSets;
    private final Set<TestTask> testTasks;

    public DefaultProjectInGradleConfiguration(File location, Set<SourceSet> sourceSets, Set<TestTask> testTasks) {
        this.location = location;
        this.sourceSets = sourceSets;
        this.testTasks = testTasks;
    }

    @Override
    public File getLocation() {
        return this.location;
    }

    @Override
    public Set<SourceSet> getSourceSets() {
        return this.sourceSets;
    }

    @Override
    public Set<TestTask> getTestTasks() {
        return this.testTasks;
    }
}
