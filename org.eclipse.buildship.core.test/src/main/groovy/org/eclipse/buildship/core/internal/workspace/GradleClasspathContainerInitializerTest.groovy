/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import static org.gradle.api.JavaVersion.VERSION_12

import org.gradle.api.JavaVersion
import org.gradle.tooling.model.eclipse.EclipseExternalDependency
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.tooling.model.eclipse.EclipseProjectDependency
import spock.lang.IgnoreIf
import spock.lang.Issue

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.jobs.IJobChangeEvent
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.core.runtime.jobs.JobChangeAdapter
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils
import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainer
import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainerUpdater
import org.eclipse.buildship.core.internal.workspace.PersistentModelBuilder

class GradleClasspathContainerInitializerTest extends ProjectSynchronizationSpecification {

    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_12) }) // Gradle 5.3 can run on Java 11 and below
    @Issue('https://github.com/eclipse/buildship/issues/893')
    def "Gradle classpath container initializer should not schedule another synchronization with different import properties"() {
        given:
        def location = dir("gradle-cp-container-init-test") {
            file "build.gradle", """
                plugins {
                    id 'java-library'
                }

                repositories.jcenter()

                dependencies {
                    implementation 'com.google.guava:guava:21.0'
                }
            """
        }
        importAndWait(location, GradleDistribution.forVersion('5.4'))
        findProject(location.name).delete(false, true, new NullProgressMonitor())


        when:
        importAndWait(location, GradleDistribution.forVersion('5.3'))

        then:
        CorePlugin.configurationManager().loadBuildConfiguration(location).gradleDistribution == GradleDistribution.forVersion('5.3')
    }
}
