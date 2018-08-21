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

package org.eclipse.buildship.core.workspace.internal

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.model.build.BuildEnvironment
import org.gradle.tooling.model.gradle.GradleBuild

import com.google.common.util.concurrent.FutureCallback

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.UnsupportedConfigurationException
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.util.gradle.Pair
import org.eclipse.buildship.core.workspace.FetchStrategy

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
        }

        when:
        importAndWait(location)

        then:
        thrown(UnsupportedConfigurationException)
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
                        compile project(':b')
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
        GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(createInheritingBuildConfiguration(location)).modelProvider.fetchModel(GradleBuild, FetchStrategy.FORCE_RELOAD, GradleConnector.newCancellationTokenSource(), new NullProgressMonitor())

        then:
        gradleBuild.rootProject.name == 'app'
    }
}
