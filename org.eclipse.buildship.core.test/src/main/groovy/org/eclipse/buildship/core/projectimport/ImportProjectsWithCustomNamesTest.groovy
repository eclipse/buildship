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

package org.eclipse.buildship.core.projectimport

import com.google.common.util.concurrent.FutureCallback

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.util.Pair

import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification

class ImportProjectsWithCustomNamesTest extends ProjectImportSpecification {

    def "Custom project naming is honored when imported from external location"() {
        setup:
        def location = folder('app')
        file('app', 'settings.gradle') << ''
        file('app', 'build.gradle') <<
        '''apply plugin: 'eclipse'
           eclipse {
               project {
                   project.name = "custom-app"
               }
           }
        '''

        when:
        executeProjectImportAndWait(location)

        then:
        findProject('custom-app')
    }

    def "Custom project naming is not honored on the root project when imported from the workspace root"() {
        setup:
        def location = workspaceFolder('app')
        workspaceFile('app', 'settings.gradle') << ''
        workspaceFile('app', 'build.gradle') <<
        '''apply plugin: 'eclipse'
           eclipse {
               project {
                   project.name = "custom-app"
               }
           }
        '''

        when:
        executeProjectImportAndWait(location)

        then:
        findProject('app')
    }

    def "Custom project naming is honored on the non-root projects even if the root is in the workspace root()"() {
        setup:
        def location = workspaceFolder('app')
        workspaceFile('app', 'settings.gradle') << 'include "sub"'
        workspaceFile('app', 'build.gradle') <<
        '''apply plugin: 'eclipse'
           eclipse {
               project {
                   project.name = "custom-app"
               }
           }
        '''
        workspaceFile('app', 'sub', 'build.gradle') <<
        '''apply plugin: 'eclipse'
           eclipse {
               project {
                   project.name = "custom-sub"
               }
           }
        '''

        when:
        executeProjectImportAndWait(location)

        then:
        allProjects().size() == 2
        findProject('app')
        findProject('custom-sub')
    }

    def "Custom project naming is not honored in the preview"() {
        setup:
        def location = folder('app')
        file('app', 'settings.gradle') << ''
        file('app', 'build.gradle') <<
        '''apply plugin: 'eclipse'
           eclipse {
               project {
                   project.name = "custom-app"
               }
           }
        '''
        FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> previewResultHandler = Mock()

        when:
        executeProjectPreviewAndWait(location, previewResultHandler)

        then:
        1 * previewResultHandler.onSuccess { it.second.rootProject.name == 'app' }
    }

}
