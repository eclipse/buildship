/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.gradle;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.Pair;

/**
 * Provides the Buildship features that are missing for a given target Gradle version.
 */
public final class MissingFeatures {

    private final GradleVersion targetVersion;

    public MissingFeatures(GradleVersion targetVersion) {
        this.targetVersion = targetVersion;
    }

    public List<Pair<GradleVersion, String>> getMissingFeatures() {
        ImmutableList.Builder<Pair<GradleVersion, String>> missingFeatures = ImmutableList.builder();
        addIfNeeded("2.7", "Test classes and methods can be run from the Editor", missingFeatures);
        addIfNeeded("2.9", "Custom project natures and build commands are added", missingFeatures);
        addIfNeeded("2.10", "Language source level is set on Java projects", missingFeatures);
        addIfNeeded("2.11", "Target bytecode version is set on Java projects", missingFeatures);
        addIfNeeded("2.11", "Java runtime is set on Java projects", missingFeatures);
        addIfNeeded("2.13", "Improved performance when loading models", missingFeatures);
        addIfNeeded("2.14", "Attributes defined for Java classpath entries", missingFeatures);
        addIfNeeded("2.14", "WTP deployment attributes defined for web projects", missingFeatures);
        addIfNeeded("3.0", "Output location, classpath containers, source folder excludes-includes and JRE name are set on Java projects", missingFeatures);
        addIfNeeded("3.0", "Java classpath customization done in 'eclipse.classpath.file.whenMerged' is synchronized", missingFeatures);
        addIfNeeded("3.3", "Support for composite builds", missingFeatures);
        addIfNeeded("5.4", "Run tasks upon synchronization", missingFeatures);
        addIfNeeded("5.5", "Import projects with overlapping names", missingFeatures);
        addIfNeeded("5.6", "Receive compile-time error when referencing test source in main source set", missingFeatures);
        addIfNeeded("5.6", "Substitute closed project references with their publications", missingFeatures);
        addIfNeeded("5.6", "Test debugging", missingFeatures);
        addIfNeeded("6.8", "Task execution in included builds", missingFeatures);
        return missingFeatures.build();
    }

    private void addIfNeeded(String version, String missingFeature, ImmutableList.Builder<Pair<GradleVersion, String>> missingFeatures) {
        GradleVersion gradleVersion = GradleVersion.version(version);
        if (this.targetVersion.getBaseVersion().compareTo(gradleVersion) < 0) {
            missingFeatures.add(createLimitation(gradleVersion, missingFeature));
        }
    }

    private Pair<GradleVersion, String> createLimitation(GradleVersion version, String missingFeature) {
        return new Pair<>(version, missingFeature + " since " + version + ".");
    }

}
