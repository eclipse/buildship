/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration

import spock.lang.AutoCleanup;
import spock.lang.Shared;
import spock.lang.Subject;

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status

import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.test.fixtures.TestEnvironment
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification


class ConfiguringGradleBuilderOnRegularProject extends WorkspaceSpecification {

    IProject project

    def setup() {
        project = newProject("sample")
    }

    def "Can configure builder"() {
        when:
        GradleProjectBuilder.configureOnProject(project)

        then:
        builderNames(project) == [GradleProjectBuilder.ID]
    }

    def "Builder configuration is idempotent"() {
        given:
        GradleProjectBuilder.configureOnProject(project)

        when:
        GradleProjectBuilder.configureOnProject(project)

        then:
        builderNames(project) == [GradleProjectBuilder.ID]
    }

    def "Can deconfigure builder"() {
        given:
        GradleProjectBuilder.configureOnProject(project)

        when:
        GradleProjectBuilder.deconfigureOnProject(project)

        then:
        builderNames(project).empty
    }

    def "Deconfiguring is a no-op if builder is not present"() {
        when:
        GradleProjectBuilder.deconfigureOnProject(project)

        then:
        builderNames(project).empty
    }

    private List<String> builderNames(IProject project) {
        project.description.buildSpec*.builderName
    }
}
