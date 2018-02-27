package eclipsebuild.jar

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

import java.util.regex.Pattern
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

abstract class BaseJarBundleTask extends DefaultTask {

    @Input
    Property<String> bundleName

    @Input
    Property<String> bundleVersion

    @Input
    Property<String> packageFilter

    @Input
    Property<String> qualifier

    @Input
    Property<String> template

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

    private ResolvedArtifact findJarArtifact(ResolvedDependency dependency) {
        dependency.moduleArtifacts.find { it.extension == 'jar' }
    }


    protected String manifestContent(File jar) {
        List<String> packageNames = packageNames(jar, this.packageFilter.get()) as List
        packageNames.sort()
        String fullVersion = "${this.bundleVersion.get()}.${this.qualifier.get()}"
        manifestFor(this.template.get(), packageNames, this.bundleVersion.get(), fullVersion)
    }

    private Set<String> packageNames(File jar, String filteredPackagesPattern) {
        def result = [] as Set
        Pattern filteredPackages = Pattern.compile(filteredPackagesPattern)
        new ZipInputStream(new FileInputStream(jar)).withCloseable { zip ->
            ZipEntry e
            while (e = zip.nextEntry) {
                if (!e.directory && e.name.endsWith(".class")) {
                    int index = e.name.lastIndexOf('/')
                    if (index < 0) index = e.name.length()
                    String packageName = e.name.substring(0, index).replace('/', '.')
                    if (!packageName.matches(filteredPackages)) {
                        result.add(packageName)
                    }
                }
            }
        }
        result
    }

    private String manifestFor(String manifestTemplate, Collection<String> packageNames, String mainVersion, String fullVersion) {
        StringBuilder manifest = new StringBuilder(manifestTemplate)

        if (!packageNames.isEmpty()) {
            String exportedPackages = packageNames.collect { " ${it};version=\"${mainVersion}\"" }.join(',\n')
            manifest.append "Export-Package:${exportedPackages}\n"
        }
        manifest.append "Bundle-Version: $fullVersion\n"
        manifest.toString()
    }
}
