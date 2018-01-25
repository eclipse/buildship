/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.gradle;

import java.util.List;

import org.gradle.util.GradleVersion;

import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.util.gradle.Pair;

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
        addIfNeeded("2.1", "Cancellation support", missingFeatures);
        addIfNeeded("2.4", "Test progress visualization", missingFeatures);
        addIfNeeded("2.5", "Build/task progress visualization", missingFeatures);
        addIfNeeded("2.5", "Transitive dependency managament", missingFeatures);
        addIfNeeded("2.6", "Tests can be run from the Executions View", missingFeatures);
        addIfNeeded("2.6", "Failed tests can be re-run from the Executions View", missingFeatures);
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
        return missingFeatures.build();
    }

    private void addIfNeeded(String version, String missingFeature, ImmutableList.Builder<Pair<GradleVersion, String>> missingFeatures) {
        GradleVersion gradleVersion = GradleVersion.version(version);
        if (this.targetVersion.getBaseVersion().compareTo(gradleVersion) < 0) {
            missingFeatures.add(createLimitation(gradleVersion, missingFeature));
        }
    }

    private Pair<GradleVersion, String> createLimitation(GradleVersion version, String missingFeature) {
        return new Pair<GradleVersion, String>(version, missingFeature + " since " + version + ".");
    }

}
