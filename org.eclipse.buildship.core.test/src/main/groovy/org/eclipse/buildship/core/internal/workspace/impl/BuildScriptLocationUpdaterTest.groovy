package org.eclipse.buildship.core.internal.workspace.impl

import org.gradle.tooling.model.GradleProject
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.tooling.model.gradle.GradleScript

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification

class BuildScriptLocationUpdaterTest extends WorkspaceSpecification {

    def "Updates build script location in persistent model"(File buildScriptFileInModel, String expectedBuildScriptPath) {
        setup:
        IProject project = newProject('sample-project')
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        EclipseProject eclipseProject = createEclipseModel(new File('.'), buildScriptFileInModel)

        when:
        BuildScriptLocationUpdater.update(eclipseProject, persistentModel, new NullProgressMonitor())

        then:
        persistentModel.buildScriptPath == new Path(expectedBuildScriptPath)

        where:
        buildScriptFileInModel                | expectedBuildScriptPath
        new File('.', 'build.gradle')         | 'build.gradle'
        new File('.', 'subdir/custom.gradle') | 'subdir/custom.gradle'
        null                                  | 'build.gradle'
    }

    EclipseProject createEclipseModel(File projectDir, File buildScriptFile) {
        Stub(EclipseProject) {
            getProjectDirectory() >> projectDir
            getGradleProject() >> Stub(GradleProject) {
                getBuildScript() >> Stub(GradleScript) {
                    getSourceFile() >> buildScriptFile
                }
            }
        }
   }
}
