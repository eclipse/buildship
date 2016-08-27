package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional
import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature
import org.eclipse.core.resources.IProject
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification;

import org.eclipse.core.runtime.NullProgressMonitor
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ProjectNatureUpdaterTest extends WorkspaceSpecification {

    def "Project nature can be set on a project"() {
        given:
        def project = newProject('sample-project')

        when:
        ProjectNatureUpdater.update(project, natures('org.eclipse.pde.UpdateSiteNature'), new NullProgressMonitor())

        then:
        project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
    }

    def "Gradle Nature is added when nature information is present"() {
        given:
        def project = newProject('sample-project')

        when:
        ProjectNatureUpdater.update(project, natures(), new NullProgressMonitor())

        then:
        project.description.natureIds.find{ it == GradleProjectNature.ID }
    }

    def "Gradle Nature is added when nature information is absent"() {
        given:
        def project = newProject('sample-project')

        when:
        ProjectNatureUpdater.update(project, Optional.absent(), new NullProgressMonitor())

        then:
        project.description.natureIds.find{ it == GradleProjectNature.ID }
    }

    def "Project natures are removed if they no longer exist in the Gradle model"() {
        given:
        def project = newProject('sample-project')

        when:
        ProjectNatureUpdater.update(project, natures('org.eclipse.pde.UpdateSiteNature'), new NullProgressMonitor())
        ProjectNatureUpdater.update(project, natures('org.eclipse.jdt.core.javanature'), new NullProgressMonitor())

        then:
        !project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
        project.description.natureIds.find{ it == 'org.eclipse.jdt.core.javanature' }
    }

    def "Manually added natures are preserved if Gradle model has no nature information"() {
        given:
        def project = newProject('sample-project')
        def description = project.description
        def manualNatures = ['org.eclipse.pde.UpdateSiteNature', 'org.eclipse.jdt.core.javanature']
        description.setNatureIds(manualNatures as String[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        ProjectNatureUpdater.update(project, Optional.absent(), new NullProgressMonitor())

        then:
        project.description.natureIds as List == manualNatures + [GradleProjectNature.ID]
    }

    def "Manually added natures are removed if Gradle model has nature information"() {
        given:
        def project = newProject('sample-project')
        def description = project.description
        description.setNatureIds(['org.eclipse.pde.UpdateSiteNature'] as String[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        ProjectNatureUpdater.update(project, natures(), new NullProgressMonitor())

        then:
        !project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
    }

    private def natures(String... natureIds) {
        Optional.of(natureIds.collect{
            def natureMock = Mock(OmniEclipseProjectNature)
            natureMock.id >> it
            natureMock
        })
    }

}
