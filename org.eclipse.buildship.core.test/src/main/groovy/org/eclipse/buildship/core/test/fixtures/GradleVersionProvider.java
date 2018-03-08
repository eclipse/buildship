/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.test.fixtures;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import org.gradle.util.GradleVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides {@link org.gradle.util.GradleVersion} instances that match the supplied Gradle version
 * range pattern.
 *
 * @author Etienne Studer
 */
public abstract class GradleVersionProvider {

    public static final String ALL = "all";
    public static final String LATEST = "latest";

    private static final Logger LOG = LoggerFactory.getLogger(GradleVersionProvider.class);

    private final ReleasedGradleVersions releasedVersions;
    private final Supplier<String> versionRangePattern;

    public GradleVersionProvider(Supplier<String> versionRangePattern) {
        this(ReleasedGradleVersions.create(), versionRangePattern);
    }

    public GradleVersionProvider(ReleasedGradleVersions releasedVersions, Supplier<String> versionRangePattern) {
        this.releasedVersions = releasedVersions;
        this.versionRangePattern = versionRangePattern;
    }

    public ImmutableList<GradleVersion> getConfiguredGradleVersions() {
        return getConfiguredGradleVersions(this.versionRangePattern.get());
    }

    public ImmutableList<GradleVersion> getConfiguredGradleVersions(String pattern) {
        LOG.debug("Applying version range pattern '{}'", pattern);

        // add matching versions to set to avoid potential duplicates (e.g. when current == latest)
        ImmutableSet<GradleVersion> configuredGradleVersions;
        if (pattern.equals(ALL)) {
            configuredGradleVersions = ImmutableSet.<GradleVersion> builder().add(GradleVersion.current()).addAll(this.releasedVersions.getAll()).build();
        } else if (pattern.equals(LATEST)) {
            configuredGradleVersions = ImmutableSet.<GradleVersion> builder().add(GradleVersion.current()).add(this.releasedVersions.getLatest()).build();
        } else if (pattern.matches("^\\d.*$")) {
            configuredGradleVersions = FluentIterable.from(Splitter.on(',').split(pattern)).transform(new Function<String, GradleVersion>() {

                @Override
                public GradleVersion apply(String input) {
                    return GradleVersion.version(input);
                }
            }).toSet();
        } else {
            throw new RuntimeException("Invalid range pattern: " + pattern + " (valid values: 'all', 'latest', or comma separated list of versions)");
        }

        return Ordering.natural().reverse().immutableSortedCopy(configuredGradleVersions);
    }

}
