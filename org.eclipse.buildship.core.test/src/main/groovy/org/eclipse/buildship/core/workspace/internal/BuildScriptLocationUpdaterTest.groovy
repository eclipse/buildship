package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path

import org.eclipse.buildship.core.omnimodel.OmniEclipseProject
import org.eclipse.buildship.core.omnimodel.OmniGradleProject
import org.eclipse.buildship.core.omnimodel.OmniGradleScript
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.util.gradle.Maybe

class BuildScriptLocationUpdaterTest extends WorkspaceSpecification {

    def "Updates build script location in persistent model"(Maybe<File> buildScriptPathInModel, String expectedBuildScriptPath) {
        setup:
        IProject project = newProject('sample-project')
        PersistentModelBuilder persistentModel = persistentModelBuilder(project)
        OmniEclipseProject eclipseProject = createEclipseModel(new File('.'), buildScriptPathInModel)

        when:
        BuildScriptLocationUpdater.update(eclipseProject, persistentModel, new NullProgressMonitor())

        then:
        persistentModel.buildScriptPath == new Path(expectedBuildScriptPath)

        where:
        buildScriptPathInModel                          | expectedBuildScriptPath
        Maybe.of(new File('.', 'build.gradle'))         | 'build.gradle'
        Maybe.of(new File('.', 'subdir/custom.gradle')) | 'subdir/custom.gradle'
        Maybe.of(null)                                  | 'build.gradle'
        Maybe.absent()                                  | 'build.gradle'
    }

    OmniEclipseProject createEclipseModel(File projectDir, Maybe<File> buildScriptFile) {

        return Stub(OmniEclipseProject) {

            getProjectDirectory() >> projectDir
            getGradleProject() >> Stub(OmniGradleProject) {
                getBuildScript() >> Maybe.of(Stub(OmniGradleScript) {
                    getSourceFile() >> (buildScriptFile.present ? buildScriptFile.get() : null)
                })
            }
        }
   }
}
