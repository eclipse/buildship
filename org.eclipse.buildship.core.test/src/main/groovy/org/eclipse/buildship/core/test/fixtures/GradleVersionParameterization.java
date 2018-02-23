/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.test.fixtures;

import java.net.URI;
import java.util.List;

import org.gradle.api.specs.Spec;
import org.gradle.util.DistributionLocator;
import org.gradle.util.GradleVersion;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.util.gradle.GradleDistribution;

/**
 * Provides parameterization for Spock that includes one or more Gradle distributions on the first
 * dimension of the data table.
 * <p/>
 * The provided Gradle version pattern supports '&gt;=nnn', '&lt;=nnn', '&gt;nnn', '&lt;nnn',
 * '=nnn', 'current', '!current', or a space-separated list of these patterns.
 * <p/>
 * See https://code.google.com/p/spock/wiki/Parameterizations
 *
 * @author Etienne Studer
 */
public abstract class GradleVersionParameterization {

    private final GradleVersionProvider gradleVersionProvider;

    public GradleVersionParameterization(GradleVersionProvider gradleVersionProvider) {
        this.gradleVersionProvider = gradleVersionProvider;
    }

    /**
     * Returns a list of triplets of all permutations of the matching Gradle versions and the
     * provided data values, where the matching Gradle versions also fall into the supplied version
     * range.
     *
     * @param gradleVersionPattern the Gradle versions to match, must not be null
     * @param dataValues the second dimension of data values, must not be null
     * @param moreDataValues the third dimension of data values, must not be null
     * @return the list of triplets, the first element of each triplet is of type
     *         {@code GradleDistribution}, never null
     */
    public ImmutableList<List<Object>> getPermutations(String gradleVersionPattern, List<Object> dataValues, List<Object> moreDataValues) {
        Preconditions.checkNotNull(gradleVersionPattern);
        Preconditions.checkNotNull(dataValues);
        Preconditions.checkNotNull(moreDataValues);

        return Combinations.getCombinations(getGradleDistributions(gradleVersionPattern), dataValues, moreDataValues);
    }

    /**
     * Returns a list of pairs of all permutations of the matching Gradle versions and the provided
     * data values, where the matching Gradle versions also fall into the supplied version range.
     *
     * @param gradleVersionPattern the Gradle versions to match, must not be null
     * @param dataValues the second dimension of data values, must not be null
     * @return the list of pairs, the first element of each pair is of type
     *         {@code GradleDistribution}, never null
     */
    public ImmutableList<List<Object>> getPermutations(String gradleVersionPattern, List<Object> dataValues) {
        Preconditions.checkNotNull(gradleVersionPattern);
        Preconditions.checkNotNull(dataValues);

        return Combinations.getCombinations(getGradleDistributions(gradleVersionPattern), dataValues);
    }

    /**
     * Returns a list of {@code GradleDistribution} that match the given Gradle version pattern,
     * where the matching Gradle versions also fall into the supplied version range.
     *
     * @param gradleVersionPattern the pattern of Gradle versions to match, must not be null
     * @return the matching Gradle distributions falling into the configured range, never null
     */
    public ImmutableList<GradleDistribution> getGradleDistributions(String gradleVersionPattern) {
        Preconditions.checkNotNull(gradleVersionPattern);

        ImmutableList<GradleVersion> gradleVersions = getGradleVersions(gradleVersionPattern);
        return convertGradleVersionsToGradleDistributions(gradleVersions);
    }

    private ImmutableList<GradleDistribution> convertGradleVersionsToGradleDistributions(ImmutableList<GradleVersion> gradleVersions) {
        return FluentIterable.from(gradleVersions).transform(new Function<GradleVersion, GradleDistribution>() {

            @Override
            public GradleDistribution apply(GradleVersion input) {
                if (input.isSnapshot()) {
                    URI distributionLocation = new DistributionLocator().getDistributionFor(input);
                    return GradleDistribution.forRemoteDistribution(distributionLocation);
                } else {
                    return GradleDistribution.forVersion(input.getVersion());
                }
            }
        }).toList();
    }

    /**
     * Returns a list of {@code GradleVersion} that match the given Gradle version pattern, where
     * the matching Gradle versions also fall into the supplied version range.
     *
     * @param gradleVersionPattern the pattern of Gradle versions to match, must not be null
     * @return the matching Gradle versions falling into the configured range, never null
     */
    ImmutableList<GradleVersion> getGradleVersions(String gradleVersionPattern) {
        Preconditions.checkNotNull(gradleVersionPattern);

        final Spec<GradleVersion> versionSpec = GradleVersionSpec.toSpec(gradleVersionPattern);
        final Spec<GradleVersion> toolingApiConstraint = GradleVersionSpec.toSpec(">=2.9");

        ImmutableList<GradleVersion> configuredGradleVersions = this.gradleVersionProvider.getConfiguredGradleVersions();
        return FluentIterable.from(configuredGradleVersions).filter(new Predicate<GradleVersion>() {

            @Override
            public boolean apply(GradleVersion input) {
                return toolingApiConstraint.isSatisfiedBy(input) && versionSpec.isSatisfiedBy(input);
            }
        }).toList();
    }

    /**
     * Provides a default implementation that derives the Gradle version range from the system
     * property <i>org.eclipse.buildship.integtest.versions</i>.
     * <p>
     * Implemented as an inner class to defer instantiation until the first time the class is
     * referenced at runtime.
     *
     * @author Etienne Studer
     */
    public static final class Default {

        /**
         * Contains the name of the system property that is queried to get the applicable version
         * range.
         */
        public static final String CROSS_VERSION_SYSTEM_PROPERTY_NAME = "integtest.versions";

        /**
         * A {@code GradleVersionParameterization} instance that reads the applicable version range
         * from {@code PublishedGradleVersions}. If the system property
         * <i>com.gradleware.tooling.integtest.versions</i> is not set, the default value
         * <i>latest</i> is used, meaning the current Gradle version and the latest Gradle version
         * are applied.
         */
        public static final GradleVersionParameterization INSTANCE = new GradleVersionParameterization(new GradleVersionProvider(new Supplier<String>() {

            @Override
            public String get() {
                return System.getProperty(CROSS_VERSION_SYSTEM_PROPERTY_NAME, GradleVersionProvider.LATEST);
            }
        }) {
        }) {
        };
    }

}
