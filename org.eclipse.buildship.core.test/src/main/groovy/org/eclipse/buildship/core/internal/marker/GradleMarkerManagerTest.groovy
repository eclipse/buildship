package org.eclipse.buildship.core.internal.marker

import org.gradle.tooling.GradleConnectionException

import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.NullProgressMonitor

import org.eclipse.buildship.core.internal.CorePlugin
import org.eclipse.buildship.core.internal.operation.ToolingApiStatus
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.workspace.GradleBuild

class GradleMarkerManagerTest extends ProjectSynchronizationSpecification {

    File projectDir
    IProject project

    def setup() {
        projectDir = dir('test-gradle-marker-manager') { file 'build.gradle', '' }
        importAndWait(projectDir)
        project = findProject('test-gradle-marker-manager')
    }

    def "Marks error location in the build script if the stacktrace contains location that is part of the wokspace"() {
        setup:
        GradleMarkerManager.addError(gradleBuild, errorInExistingBuildScript)

        expect:
        numOfGradleErrorMarkers == 1
        gradleErrorMarkers[0].resource.fullPath.toPortableString() == '/test-gradle-marker-manager/build.gradle'
        gradleErrorMarkers[0].getAttribute(IMarker.LINE_NUMBER, 0) == 1
    }

    def "Marks root build script if the stacktrace contains location that is not part of the workspace"() {
        setup:
        GradleMarkerManager.addError(gradleBuild, errorInNonexistingBuildScript)

        expect:
        numOfGradleErrorMarkers == 1
        gradleErrorMarkers[0].resource.fullPath.toPortableString() == '/test-gradle-marker-manager/build.gradle'
        gradleErrorMarkers[0].getAttribute(IMarker.LINE_NUMBER, 0) == 0
    }

    def "Marks root project build script if the stacktrace contains location and no build script available"() {
        setup:
        project.getFile('build.gradle').delete(true, new NullProgressMonitor())
        GradleMarkerManager.addError(gradleBuild, errorInExistingBuildScript)

        expect:
        numOfGradleErrorMarkers == 1
        gradleErrorMarkers[0].resource.fullPath.toPortableString() == '/test-gradle-marker-manager'
    }

    def "Marks workspace root if the stacktrace contains location and no target nor root project available"() {
        setup:
        GradleBuild savedGradleBuild = gradleBuild;
        project.delete(true, new NullProgressMonitor())
        GradleMarkerManager.addError(savedGradleBuild, errorInExistingBuildScript)

        expect:
        numOfGradleErrorMarkers == 1
        gradleErrorMarkers[0].resource.fullPath.toPortableString() == '/'
    }

    def "Marks root project build script if the stacktrace doesn't reference build script and root build script is available"() {
        setup:
        GradleBuild savedGradleBuild = gradleBuild;
        GradleMarkerManager.addError(savedGradleBuild, connectionProblem)

        expect:
        numOfGradleErrorMarkers == 1
        gradleErrorMarkers[0].resource.fullPath.toPortableString() == '/test-gradle-marker-manager/build.gradle'
    }

    def "Marks root project if the stacktrace doesn't reference build script and only root project is available, root build script is missing"() {
        setup:
        project.getFile('build.gradle').delete(true, new NullProgressMonitor())
        GradleMarkerManager.addError(gradleBuild, connectionProblem)

        expect:
        numOfGradleErrorMarkers == 1
        gradleErrorMarkers[0].resource.fullPath.toPortableString() == '/test-gradle-marker-manager'
    }

    def "Marks workspace root if the stacktrace doesn't reference build script and no target nor root project available-"() {
        setup:
        GradleBuild savedGradleBuild = gradleBuild;
        project.delete(true, new NullProgressMonitor())
        GradleMarkerManager.addError(savedGradleBuild, connectionProblem)

        expect:
        numOfGradleErrorMarkers == 1
        gradleErrorMarkers[0].resource.fullPath.toPortableString() == '/'
    }

    private GradleBuild getGradleBuild() {
        CorePlugin.gradleWorkspaceManager().getGradleBuild(project).get()
    }

    private ToolingApiStatus getErrorInExistingBuildScript() {
        ToolingApiStatus.from("", new Exception(new Exception("Build file '$projectDir.canonicalPath${System.getProperty('file.separator')}build.gradle' line: 1")))
    }

    private ToolingApiStatus getErrorInNonexistingBuildScript() {
        ToolingApiStatus.from("", new Exception(new Exception("Build file '/path/to/script' line: 1")))
    }

    private ToolingApiStatus getConnectionProblem() {
        ToolingApiStatus.from("", new GradleConnectionException(''))
    }
}
