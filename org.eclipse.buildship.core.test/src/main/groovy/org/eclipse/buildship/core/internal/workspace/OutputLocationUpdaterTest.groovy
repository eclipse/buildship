/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import org.gradle.tooling.model.eclipse.EclipseOutputLocation
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.tooling.model.eclipse.EclipseSourceDirectory

import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject

import org.eclipse.buildship.core.ProjectContext
import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils

class OutputLocationUpdaterTest extends WorkspaceSpecification {

    IJavaProject javaProject

    def setup() {
        javaProject = newJavaProject("output-loc-test")
        javaProject.setRawClasspath([] as IClasspathEntry[], new NullProgressMonitor())
    }

    def "Updates output location"(String defaultOutput, String sourceDirOutput) {
        when:
        OutputLocationUpdater.update(projectContext(), javaProject, eclipseProject(defaultOutput, sourceDirOutput), new NullProgressMonitor())

        then:
        javaProject.outputLocation.toPortableString() == "/output-loc-test/$defaultOutput"

        where:
        defaultOutput | sourceDirOutput
        'target'      | 'bin/main'
        'target'      | 'target'
    }

    def "Uses fallback output location when source directory has nested output location"() {
        when:
        OutputLocationUpdater.update(projectContext(), javaProject, eclipseProject("target", "target/main"), new NullProgressMonitor())

        then:
        javaProject.outputLocation.toPortableString() == '/output-loc-test/target-default'

    }

    private ProjectContext projectContext() {
        ProjectContext projectContext = Mock(ProjectContext)
        projectContext.project >> javaProject.project
        projectContext
    }

    private EclipseProject eclipseProject(String output, String sourceDirOutput) {

        Stub(EclipseProject) {
            getOutputLocation() >> Stub(EclipseOutputLocation) {
                 getPath() >> output
             }
            getSourceDirectories() >> ModelUtils.asDomainObjectSet([
                Stub(EclipseSourceDirectory) {
                    getOutput() >> sourceDirOutput
                }
            ])
        }
    }
}
