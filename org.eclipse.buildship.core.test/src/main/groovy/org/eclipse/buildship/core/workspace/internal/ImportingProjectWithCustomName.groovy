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

import com.google.common.util.concurrent.FutureCallback

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuild
import com.gradleware.tooling.toolingmodel.util.Pair

import org.eclipse.core.resources.IProject
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.notification.UserNotification
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

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
        Logger logger = Mock(Logger)
        environment.registerService(Logger, logger)
        environment.registerService(UserNotification, Mock(UserNotification))

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
        allProjects().isEmpty()
        1 * logger.warn(*_)
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

        FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuild>> previewResultHandler = Mock()

        when:
        previewAndWait(location, previewResultHandler)

        then:
        1 * previewResultHandler.onSuccess { it.second.rootProject.name == 'app' }
    }

}
