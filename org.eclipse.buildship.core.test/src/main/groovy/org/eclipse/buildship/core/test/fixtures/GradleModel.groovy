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

package org.eclipse.buildship.core.test.fixtures

import org.gradle.tooling.GradleConnector

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild
import com.gradleware.tooling.toolingmodel.OmniEclipseProject
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes
import com.gradleware.tooling.toolingmodel.repository.ModelRepository
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes

import org.eclipse.buildship.core.CorePlugin


/**
 * Helper class to load Gradle models.
 */
abstract class GradleModel {

    private FixedRequestAttributes attributes
    private OmniEclipseGradleBuild build

    GradleModel(FixedRequestAttributes attributes, OmniEclipseGradleBuild model) {
        this.attributes = attributes
        this.build = model
    }

    /**
     * The request attributes used to load the Gradle model.
     *
     * @return the request attributes
     */
    public FixedRequestAttributes getAttributes() {
        attributes
    }

    /**
     * The OmniEclipseGradleBuild model of the loaded project.
     *
     * @return the OmniEclipseGradleBuild model
     */
    public OmniEclipseGradleBuild getBuild() {
        build
    }

    /**
     * The OmniEclipseProject model of the loaded project.
     *
     * @return the OmniEclipseProject model
     */
    public OmniEclipseProject eclipseProject(String name) {
        build.rootEclipseProject.all.find { it.name == name }
    }

    /**
     * Loads the Gradle project from the target folder.
     *
     * @param rootProjectFolder the folder from where the Gradle model should be loaded
     */
    static GradleModel fromProject(File rootProjectFolder) {
        FixedRequestAttributes attributes = new FixedRequestAttributes(rootProjectFolder, null, GradleDistribution.fromBuild(), null, [], [])
        ModelRepository modelRepository = CorePlugin.modelRepositoryProvider().getModelRepository(attributes)
        OmniEclipseGradleBuild eclipseGradleBuild = modelRepository.fetchEclipseGradleBuild(new TransientRequestAttributes(false, System.out, System.err, System.in, [], [], GradleConnector.newCancellationTokenSource().token()), FetchStrategy.LOAD_IF_NOT_CACHED)
        new GradleModel(attributes, eclipseGradleBuild) {}
    }
}
