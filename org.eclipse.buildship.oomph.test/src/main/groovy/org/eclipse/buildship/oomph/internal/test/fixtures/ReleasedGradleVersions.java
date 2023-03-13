/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph.internal.test.fixtures;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import org.eclipse.buildship.core.internal.util.gradle.GradleVersion;
import org.eclipse.buildship.core.internal.util.gradle.PublishedGradleVersions;

/**
 * Provides released versions of Gradle. Typically, the list of provided Gradle versions is
 * identical with the set of Gradle versions supported by the application.
 *
 * @author Etienne Studer
 */
public abstract class ReleasedGradleVersions {

    private final ImmutableList<GradleVersion> versions;

    private ReleasedGradleVersions(ImmutableList<GradleVersion> versions) {
        this.versions = versions;
    }

    public ImmutableList<GradleVersion> getAll() {
        return this.versions;
    }

    public GradleVersion getLatest() {
        return getAll().get(0);
    }

    /**
     * Creates a new instances from {@code PublishedGradleVersions}.
     *
     * @return the new instance
     */
    public static ReleasedGradleVersions create() {
        PublishedGradleVersions publishedGradleVersions = PublishedGradleVersions.create(PublishedGradleVersions.LookupStrategy.REMOTE);
        ImmutableSet<GradleVersion> nonMilestoneReleases = FluentIterable.from(publishedGradleVersions.getVersions()).filter(new Predicate<GradleVersion>() {

            @Override
            public boolean apply(GradleVersion version) {
                return !version.getVersion().contains("-milestone-");
            }
        }).toSet();
        return createFromGradleVersions(nonMilestoneReleases);
    }

    /**
     * Creates a new instances from the given version strings.
     *
     * @param versionStrings the version strings
     * @return the new instance
     */
    public static ReleasedGradleVersions createFrom(Set<String> versionStrings) {
        return createFromGradleVersions(FluentIterable.from(versionStrings).transform(new Function<String, GradleVersion>() {

            @Override
            public GradleVersion apply(String version) {
                return GradleVersion.version(version);
            }
        }).toSet());
    }

    private static ReleasedGradleVersions createFromGradleVersions(Set<GradleVersion> versionStrings) {
        Preconditions.checkNotNull(versionStrings);
        Preconditions.checkState(!versionStrings.isEmpty());

        // order version strings from newest to oldest
        ImmutableList<GradleVersion> versions = Ordering.natural().reverse().immutableSortedCopy(versionStrings);

        return new ReleasedGradleVersions(versions) {
        };
    }

}
