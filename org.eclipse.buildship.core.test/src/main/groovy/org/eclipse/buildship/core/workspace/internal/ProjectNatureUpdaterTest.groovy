package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.omnimodel.OmniEclipseProjectNature
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification;

class ProjectNatureUpdaterTest extends WorkspaceSpecification {

    def "Project nature can be set on a project"() {
        given:
        IProject project = newProject('sample-project')

        when:
        executeProjectNatureUpdater(project, persistentModelBuilder(project), 'org.eclipse.pde.UpdateSiteNature')

        then:
        hasNature(project, 'org.eclipse.pde.UpdateSiteNature')
    }

    def "Gradle Nature is added when nature information is present"() {
        given:
        IProject project = newProject('sample-project')

        when:
        executeProjectNatureUpdater(project, persistentModelBuilder(project))

        then:
        hasNature(project, GradleProjectNature.ID)
    }

    def "Gradle Nature is added when nature information is absent"() {
        given:
        IProject project = newProject('sample-project')

        when:
        executeProjectNatureUpdaterWithAbsentModel(project)

        then:
        hasNature(project, GradleProjectNature.ID)
    }

    def "Project natures are removed if they were added by Gradle and no longer exist in the model"() {
        given:
        IProject project = newProject('sample-project')
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        executeProjectNatureUpdater(project, persistentModel, 'org.eclipse.pde.UpdateSiteNature')

        then:
        hasNature(project, 'org.eclipse.pde.UpdateSiteNature')

        when:
        executeProjectNatureUpdater(project, persistentModelBuilder(persistentModel.build()), 'org.eclipse.jdt.core.javanature')

        then:
        !hasNature(project, 'org.eclipse.pde.UpdateSiteNature')
        hasNature(project, 'org.eclipse.jdt.core.javanature')
    }

    def "Manually added natures are preserved if Gradle model has no nature information"() {
        given:
        IProject project = newProject('sample-project')
        IProjectDescription description = project.description
        List userNatures = ['org.eclipse.pde.UpdateSiteNature', 'org.eclipse.jdt.core.javanature']
        description.setNatureIds(userNatures as String[])
        project.setDescription(description, new NullProgressMonitor())

        when:
        executeProjectNatureUpdaterWithAbsentModel(project)

        then:
        naturesOf(project) == [GradleProjectNature.ID] + userNatures
    }

    def "Manually added natures are preserved if they were added manually"() {
        given:
        IProject project = newProject('sample-project')
        IProjectDescription description = project.description
        List manualNatures = ['org.eclipse.pde.UpdateSiteNature']
        description.setNatureIds(manualNatures as String[])
        project.setDescription(description, new NullProgressMonitor())
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)

        when:
        executeProjectNatureUpdater(project, persistentModel)

        then:
        hasNature(project, 'org.eclipse.pde.UpdateSiteNature')

        when:
        persistentModel = persistentModelBuilder(persistentModel.build())
        executeProjectNatureUpdater(project, persistentModel, 'org.eclipse.pde.UpdateSiteNature')

        then:
        hasNature(project, 'org.eclipse.pde.UpdateSiteNature')

        when:
        persistentModel = persistentModelBuilder(persistentModel.build())
        executeProjectNatureUpdater(project, persistentModel)

        then:
        hasNature(project, 'org.eclipse.pde.UpdateSiteNature')
    }

    private void executeProjectNatureUpdaterWithAbsentModel(IProject project) {
        ProjectNatureUpdater.update(project, Optional.absent(), persistentModelBuilder(project), new NullProgressMonitor())
    }

    private void executeProjectNatureUpdater(IProject project, PersistentModelBuilder persistentModel, String... natures) {
        List<OmniEclipseProjectNature> modelNatures = natures.collect { String natureId ->
            OmniEclipseProjectNature nature = Mock(OmniEclipseProjectNature)
            nature.id >> natureId
            nature
        }
        ProjectNatureUpdater.update(project, Optional.of(modelNatures), persistentModel, new NullProgressMonitor())
    }

    private List<String> naturesOf(IProject project) {
        project.description.natureIds as List
    }

    private boolean hasNature(IProject project, String natureId) {
        project.description.natureIds.find { it == natureId }
    }

    private Optional<OmniEclipseProjectNature> absentModelNatures() {
        Optional.absent()
    }

    private Optional<OmniEclipseProjectNature> modelNatures(String... natureIds) {
        Optional.of(natureIds.collect { String natureId ->
            OmniEclipseProjectNature nature = Mock(OmniEclipseProjectNature)
            nature.id >> natureId
            nature
        })
    }

}
