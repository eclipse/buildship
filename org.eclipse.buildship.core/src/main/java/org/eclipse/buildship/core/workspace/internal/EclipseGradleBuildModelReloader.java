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

package org.eclipse.buildship.core.workspace.internal;

import java.util.List;
import java.util.Set;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessStreams;

/**
 * Finds the root workspace project for the target projects and reloads their corresponding
 * {@link OmniEclipseGradleBuild} model.
 */
public final class EclipseGradleBuildModelReloader {

    private final List<IProject> projects;
    private final CancellationToken token;

    private EclipseGradleBuildModelReloader(List<IProject> projects, CancellationToken token) {
        this.projects = ImmutableList.copyOf(projects);
        this.token = Preconditions.checkNotNull(token);
    }

    public ImmutableSet<OmniEclipseGradleBuild> reloadRootEclipseModels() {
        Set<FixedRequestAttributes> attributesFromConfiguration = readRequestAttributesFromProjectConfiguration(this.projects);
        return FluentIterable.from(attributesFromConfiguration).transform(new Function<FixedRequestAttributes, OmniEclipseGradleBuild>() {

            @Override
            public OmniEclipseGradleBuild apply(FixedRequestAttributes attributes) {
                return foreceReloadEclipseGradleBuild(attributes);
            }
        }).toSet();
    }

    private Set<FixedRequestAttributes> readRequestAttributesFromProjectConfiguration(List<IProject> projects) {
        return FluentIterable.from(projects).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

    private OmniEclipseGradleBuild foreceReloadEclipseGradleBuild(FixedRequestAttributes requestAttributes) {
        ProcessStreams stream = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        return CorePlugin.modelRepositoryProvider().getModelRepository(requestAttributes)
                .fetchEclipseGradleBuild(new TransientRequestAttributes(false, stream.getOutput(), stream.getError(), stream.getInput(), ImmutableList.<ProgressListener>of(),
                        ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), this.token), FetchStrategy.FORCE_RELOAD);
    }

    public static EclipseGradleBuildModelReloader from(List<IProject> projects, CancellationToken token) {
        return new EclipseGradleBuildModelReloader(projects, token);
    }
}
