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

package org.eclipse.buildship.core.internal.gradle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.Pair;

/**
 * MissingFeatures test.
 * <p/>
 * Note: This test had to be converted to JUnit test from Spock because it threw a
 * ClassNotFoundError upon execution: it tried to load the The GradleVersion$Stage class which is
 * package private and causes problems. To be investigate why it happens and how should be fixed.
 *
 */
public class MissingFeaturesTest {

    @Test
    public void noLimitationsWhenUsingTheCurrentVersionUsedByBuildship() {
        MissingFeatures missingFeatures = new MissingFeatures(GradleVersion.current());
        List<Pair<GradleVersion, String>> details = missingFeatures.getMissingFeatures();
        assertTrue(details.isEmpty());
    }

    @Test
    public void noLimitationsWhenUsingTheBaseVersionOfTheVersionUseByBuildship() {
        MissingFeatures missingFeatures = new MissingFeatures(GradleVersion.current().getBaseVersion());
        List<Pair<GradleVersion, String>> details = missingFeatures.getMissingFeatures();
        assertTrue(details.isEmpty());
    }

    @Test
    public void someLimitationWhenUsingFinalVersionOf24() {
        MissingFeatures missingFeatures = new MissingFeatures(GradleVersion.version("2.4"));
        List<Pair<GradleVersion, String>> details = missingFeatures.getMissingFeatures();
        assertEquals(details.size(), 14);
    }

    @Test
    public void someLimitationWhenUsingSnapshotVersionOf24() {
        MissingFeatures missingFeatures = new MissingFeatures(GradleVersion.version("2.4-20150101053008+0000"));
        List<Pair<GradleVersion, String>> details = missingFeatures.getMissingFeatures();
        assertEquals(details.size(), 14);
    }

}
