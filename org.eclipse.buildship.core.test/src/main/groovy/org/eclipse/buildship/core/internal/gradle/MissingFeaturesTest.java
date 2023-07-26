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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;


import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.Pair;
import org.junit.jupiter.api.Test;

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
    public void someLimitationWhenUsingFinalVersionOf26() {
        MissingFeatures missingFeatures = new MissingFeatures(GradleVersion.version("2.6"));
        List<Pair<GradleVersion, String>> details = missingFeatures.getMissingFeatures();
        assertEquals(details.size(), 17);
    }

    @Test
    public void someLimitationWhenUsingSnapshotVersionOf26() {
        MissingFeatures missingFeatures = new MissingFeatures(GradleVersion.version("2.6-20150101053008+0000"));
        List<Pair<GradleVersion, String>> details = missingFeatures.getMissingFeatures();
        assertEquals(details.size(), 17);
    }

}
