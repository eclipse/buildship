package org.eclipse.buildship.core.internal.workspace.impl

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
