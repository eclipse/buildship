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

import java.io.File;
import java.util.Set;

// TODO this is the plan
// Collect all SourceSet(runtimeClasspath, allSource.srcDirs) instances
// Collect MultiMap<SourceSet, String> sourceSetToTestPathsMapping -> all items specify Predicate: source set runtime classpath contains at least one item from testClassesDirs
// Set<SourceSet> sourceSetsOf(IResource resource) -> return all source sets where the srcDir is a prefix for resource.getPath

public interface SourceSet {

    String getName();

    Set<File> getRuntimeClasspath();

    Set<File> getSrcDirs();
}
