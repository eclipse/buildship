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

import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.util.Pair;
import org.gradle.util.GradleVersion;

import java.util.List;

/**
 * Provides the limitations of the Buildship functionality for a given target Gradle version.
 */
public final class Limitations {

    private final GradleVersion targetVersion;

    public Limitations(GradleVersion targetVersion) {
        this.targetVersion = targetVersion;
    }

    public List<Pair<GradleVersion, String>> getLimitations() {
        ImmutableList.Builder<Pair<GradleVersion, String>> limitations = ImmutableList.builder();
        addIfNeeded("2.1", "No cancellation support", limitations);
        addIfNeeded("2.4", "No test progress visualization", limitations);
        addIfNeeded("2.5", "No build/task progress visualization", limitations);
        addIfNeeded("2.5", "No improved project classpath generation with all transitive dependencies as direct dependencies", limitations);
        addIfNeeded("2.6", "No running of tests from the Executions View", limitations);
        addIfNeeded("2.6", "No rerunning of failed tests from the Executions View", limitations);
        addIfNeeded("2.7", "No running of test classes and methods from the Editor", limitations);
        addIfNeeded("2.9", "No custom project natures and build commands applied", limitations);
        addIfNeeded("2.10", "No source language level set for Java projects", limitations);
        return limitations.build();
    }

    private void addIfNeeded(String version, String limitation, ImmutableList.Builder<Pair<GradleVersion, String>> limitations) {
        GradleVersion gradleVersion = GradleVersion.version(version);
        if (this.targetVersion.getBaseVersion().compareTo(gradleVersion) < 0) {
            limitations.add(createLimitation(gradleVersion, limitation));
        }
    }

    private Pair<GradleVersion, String> createLimitation(GradleVersion version, String limitation) {
        return new Pair<GradleVersion, String>(version, limitation + " in Gradle versions <" + version.getVersion() + ".");
    }

}
