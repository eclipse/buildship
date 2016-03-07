/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.configuration.internal;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.configuration.ProjectConfiguration;

/**
 * Persistence implementation aware of legacy, json-based project configuration format.
 */
final class LegacyCleaningProjectConfigurationPersistence implements ProjectConfigurationPersistence {

    private final ProjectConfigurationPersistence delegate;

    LegacyCleaningProjectConfigurationPersistence(ProjectConfigurationPersistence delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public void saveProjectConfiguration(ProjectConfiguration configuration, IProject project) {
        LegacyProjectConfigurationHandler.cleanupLegacyConfiguration(project);
        this.delegate.saveProjectConfiguration(configuration, project);
    }

    @Override
    public void deleteProjectConfiguration(IProject project) {
        this.delegate.deleteProjectConfiguration(project);
    }

    @Override
    public ProjectConfiguration readProjectConfiguration(IProject project) {
        if (LegacyProjectConfigurationHandler.hasLegacyConfiguration(project)) {
            return LegacyProjectConfigurationHandler.readProjectConfiguration(project);
        } else {
            return this.delegate.readProjectConfiguration(project);
        }
    }

}
