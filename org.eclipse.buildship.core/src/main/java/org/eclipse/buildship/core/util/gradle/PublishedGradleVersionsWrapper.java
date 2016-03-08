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

package org.eclipse.buildship.core.util.gradle;

import java.util.List;

import org.eclipse.buildship.core.CorePlugin;

import org.gradle.util.GradleVersion;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions;

/**
 * Wraps the {@link PublishedGradleVersions} functionality to handle all exceptions gracefully. If an exception occurs while
 * calling the underlying {@link PublishedGradleVersions} instance, empty version information is provided. This handles, for
 * example, those scenarios where the versions cannot be retrieved because the user is behind a proxy or offline.
 */
public final class PublishedGradleVersionsWrapper {

    private final Optional<PublishedGradleVersions> publishedGradleVersions;

    public PublishedGradleVersionsWrapper() {
        this.publishedGradleVersions = create();
    }

    private Optional<PublishedGradleVersions> create() {
        try {
            return Optional.of(PublishedGradleVersions.create(true));
        } catch (Exception e) {
            CorePlugin.logger().warn("Cannot retrieve published Gradle version.", e);
            return  Optional.absent();
        }
    }

    public List<GradleVersion> getVersions() {
        return this.publishedGradleVersions.isPresent() ? this.publishedGradleVersions.get().getVersions() : ImmutableList.<GradleVersion> of(GradleVersion.current());
    }

}
