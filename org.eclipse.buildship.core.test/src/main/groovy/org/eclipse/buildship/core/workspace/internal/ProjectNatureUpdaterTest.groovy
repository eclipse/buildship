package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniEclipseProjectNature

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.test.fixtures.EclipseProjects

class ProjectNatureUpdaterTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "Project nature can be set on a project"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())

        when:
        ProjectNatureUpdater.update(project, natures('org.eclipse.pde.UpdateSiteNature'), new NullProgressMonitor())

        then:
        project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
    }

    def "Project natures are removed if they no longer exist in the Gradle model"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())

        when:
        ProjectNatureUpdater.update(project, natures('org.eclipse.pde.UpdateSiteNature'), new NullProgressMonitor())
        ProjectNatureUpdater.update(project, natures('org.eclipse.jdt.core.javanature'), new NullProgressMonitor())

        then:
        !project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
        project.description.natureIds.find{ it == 'org.eclipse.jdt.core.javanature' }
    }

    def "Manually added natures are preserved"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())
        def description = project.description
        description.setNatureIds([
            'org.eclipse.pde.UpdateSiteNature'] as String[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        ProjectNatureUpdater.update(project, natures(), new NullProgressMonitor())

        then:
        project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
    }

    def "Project natures that were previously defined manually are transformed to model source folders"() {
        given:
        def project = EclipseProjects.newProject('sample-project', tempFolder.newFolder())
        def description = project.description
        description.setNatureIds([
            'org.eclipse.pde.UpdateSiteNature'] as String[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        ProjectNatureUpdater.update(project, natures('org.eclipse.pde.UpdateSiteNature'), new NullProgressMonitor())
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
