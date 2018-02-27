package eclipsebuild.jar

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.Input

abstract class SingleDependencyProjectTask extends DefaultTask {

    @Input
    Configuration pluginConfiguration

    protected File getDependencyJar() {
        ResolvedArtifact jarArtifact = findJarArtifact(getResolvedDependency())
        if (jarArtifact == null) {
            throw new RuntimeException("Project $project.name does not have dependency jar")
        }
        jarArtifact.file
    }

    private ResolvedDependency getResolvedDependency() {
        List dependencies = pluginConfiguration.resolvedConfiguration.firstLevelModuleDependencies as List
        if (dependencies.size() != 1) {
            throw new RuntimeException("Project $project.name has more than one dependency")
        }
        dependencies[0]
    }

    ResolvedArtifact findJarArtifact(ResolvedDependency dependency) {
        dependency.moduleArtifacts.find { it.extension == 'jar' }
    }
}
