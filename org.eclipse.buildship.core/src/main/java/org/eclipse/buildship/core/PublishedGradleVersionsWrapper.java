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

package org.eclipse.buildship.core;

import java.util.List;

import org.gradle.util.GradleVersion;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingutils.distribution.PublishedGradleVersions;

/**
 * Wraps the {@link PublishedGradleVersions} functionality enabling plug-in activation even if the
 * user is offline or behind a firewall and can't download the version information from the
 * Internet.
 */
public final class PublishedGradleVersionsWrapper {

    private Optional<PublishedGradleVersions> publishedGradleVersions;

    public PublishedGradleVersionsWrapper() {
        try {
            this.publishedGradleVersions = Optional.of(PublishedGradleVersions.create(true));
        } catch (Exception e) {
            e.printStackTrace();
            this.publishedGradleVersions = Optional.absent();
        }
    }

    public List<GradleVersion> getVersions() {
        // TODO (donat) retry instantiating if not present
        return this.publishedGradleVersions.isPresent() ? this.publishedGradleVersions.get().getVersions() : ImmutableList.<GradleVersion> of();
    }

}
