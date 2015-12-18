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

import com.gradleware.tooling.toolingmodel.util.Pair;
import org.gradle.util.GradleVersion;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Limitations test.
 * <p/>
 * Note: This test had to be converted to JUnit test from Spock because it threw a
 * ClassNotFoundError upon execution: it tried to load the The GradleVersion$Stage class which is
 * package private and causes problems. To be investigate why it happens and how should be fixed.
 *
 */
public class LimitationsTest {

    @Test
    public void noLimitationsWhenUsingTheCurrentVersionUsedByBuildship() {
        Limitations limitations = new Limitations(GradleVersion.current());
        List<Pair<GradleVersion, String>> limitationDetails = limitations.getLimitations();
        assertTrue(limitationDetails.isEmpty());
    }

    @Test
    public void noLimitationsWhenUsingTheBaseVersionOfTheVersionUseByBuildship() {
        Limitations limitations = new Limitations(GradleVersion.current().getBaseVersion());
        List<Pair<GradleVersion, String>> limitationDetails = limitations.getLimitations();
        assertTrue(limitationDetails.isEmpty());
    }

    @Test
    public void someLimitationWhenUsingFinalVersionOf24() {
        Limitations limitations = new Limitations(GradleVersion.version("2.4"));
        List<Pair<GradleVersion, String>> limitationDetails = limitations.getLimitations();
        assertEquals(limitationDetails.size(), 8);
    }

    @Test
    public void someLimitationWhenUsingSnapshotVersionOf24() {
        Limitations limitations = new Limitations(GradleVersion.version("2.4-20150101053008+0000"));
        List<Pair<GradleVersion, String>> limitationDetails = limitations.getLimitations();
        assertEquals(limitationDetails.size(), 8);
    }

}
