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

import spock.lang.Issue

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

@Issue("https://bugs.eclipse.org/bugs/show_bug.cgi?id=497753")
class ImportingProjectWithCustomNature extends ProjectSynchronizationSpecification {

    // registered in the org.eclipse.buildship.core.test/fragment.xml file
    public static class JavaExtensionNature implements IProjectNature {
        void configure() { }
        void deconfigure() { }
        IProject getProject() { }
        void setProject(IProject project) { }
    }

    def "Can import project with custom project nature that depends on another nature"() {
        setup:
        Logger logger = Mock(Logger)
        environment.registerService(Logger, logger)
        File location = dir('project-with-custom-nature') {
            file 'build.gradle', '''
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        natures 'org.eclipse.buildship.core.javaextensionnature', 'org.eclipse.jdt.core.javanature'
                    }
                }
            '''
        }

        when:
        importAndWait(location)

        then:
        0 * logger.error(*_)
    }
}
