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

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.gradle.GradleBuild

import com.google.common.util.concurrent.FutureCallback

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.util.gradle.Pair

class ImportingProjectWithCustomName extends ProjectSynchronizationSpecification {

    def "Custom project naming is honored when imported from external location"() {
        setup:
        def location = dir('app') {
            file 'build.gradle', '''
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        project.name = "custom-app"
                    }
                }
            '''
        }

        when:
        importAndWait(location)

        then:
        findProject('custom-app')
    }

    def "Custom project naming is disallowed on the root project when imported from the workspace root"() {
        setup:
        def location = workspaceDir('app') {
            file 'build.gradle', '''
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        project.name = "custom-app"
                    }
                }
            '''
            file 'settings.gradle', ''
        }

        when:
        SynchronizationResult result = tryImportAndWait(location)

        then:
        result.status.severity == IStatus.WARNING
        result.status.exception instanceof UnsupportedConfigurationException
        findProject('app')
    }

    def "Custom project naming is honoured on the non-root projects when the root is under the workspace root"() {
        setup:
        def location = workspaceDir('app2') {
            file 'settings.gradle', "include 'sub'"
            dir('sub') {
                file 'build.gradle', '''
                    apply plugin: 'eclipse'
                    eclipse {
                        project {
                            project.name = "custom-sub"
                        }
                    }
                '''
            }
        }

        when:
        importAndWait(location)

        then:
        allProjects().size() == 2
        findProject('app2')
        findProject('custom-sub')
    }

    def "Dependencies on projects with custom names are resolved correctly"() {
        setup:
        def root = dir('root') {
            file 'settings.gradle', "include 'a', 'b'"
            a {
                file 'build.gradle', '''
                    apply plugin: 'java'
                    dependencies {
                        implementation project(':b')
                    }
                '''
            }
            b {
                file 'build.gradle', '''
                    apply plugin: 'java'
                    apply plugin: 'eclipse'
                    eclipse.project.name = "c"
                '''
            }
        }


        when:
        importAndWait(root)

        then:
        IProject sub = findProject("a")
        IJavaProject javaProject = JavaCore.create(sub)
        javaProject.getResolvedClasspath(true).find {
            it.path.toString() == "/c"
        }
    }

    def "Custom project naming is not honored in the preview"() {
        setup:
        def location = dir('app') {
            file 'build.gradle', '''
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        project.name = "custom-app"
                    }
                }
            '''
        }

        FutureCallback<Pair<BuildEnvironment, GradleBuild>> previewResultHandler = Mock()

        when:
        GradleBuild gradleBuild = CorePlugin.internalGradleWorkspace().getGradleBuild(createInheritingBuildConfiguration(location)).modelProvider.fetchModel(GradleBuild, FetchStrategy.FORCE_RELOAD, GradleConnector.newCancellationTokenSource(), new NullProgressMonitor())

        then:
        gradleBuild.rootProject.name == 'app'
    }
}
