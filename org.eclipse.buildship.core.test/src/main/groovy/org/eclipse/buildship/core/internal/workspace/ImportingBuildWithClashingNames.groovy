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

import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ImportingBuildWithClashingNames extends ProjectSynchronizationSpecification {

    def "Conflicting project names from the same build are de-duped"() {
        setup:
        def project = dir('project') {
            dir 'project'
            file 'settings.gradle', "include 'project'"
        }

        when:
        importAndWait(project)

        then:
        allProjects().size() == 2
        findProject('project').location.toFile() == project.canonicalFile
        findProject('project-project').location.toFile() == new File(project, 'project').canonicalFile
    }

}
