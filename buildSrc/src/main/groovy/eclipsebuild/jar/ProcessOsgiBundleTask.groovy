package eclipsebuild.jar

import java.util.regex.Pattern
import java.util.zip.*

import org.gradle.api.*
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.util.ConfigureUtil

class ProcessOsgiBundleTask extends DefaultTask {

    @Input
    Configuration pluginConfiguration

    @Input
    Property<String> bundleName

    @Input
    Property<String> bundleVersion

    @Input
    Property<String> qualifier

    @Input
    Property<String> template

    @Input
    Property<String> packageFilter

    Closure resources = {}

    @OutputDirectory
    File target

    @TaskAction
    void processOsgiBundles() {
        pluginDependencies().each { ResolvedDependency dependency ->
            ResolvedArtifact jarArtifact = findJarArtifact(dependency)
            if (jarArtifact) {
                createNewBundle(jarArtifact)
            }
        }
    }

    Set<ResolvedDependency> pluginDependencies() {
        pluginConfiguration.resolvedConfiguration.firstLevelModuleDependencies
    }

    void createNewBundle(ResolvedArtifact jar) {
        List<String> packageNames = packageNames(jar.file, this.packageFilter.get()) as List
        packageNames.sort()
        String fullVersion = "${this.bundleVersion.get()}.${this.qualifier.get()}"
        String manifest = manifestFor(this.template.get(), packageNames, this.bundleVersion.get(), fullVersion)

        File extraResources = project.file("${project.buildDir}/tmp/bundle-resources/${this.bundleName.get().replace(':', '.')}")
        File manifestFile = new File(extraResources, '/META-INF/MANIFEST.MF')
        manifestFile.parentFile.mkdirs()
        manifestFile.text = manifest

        project.copy {
            with ConfigureUtil.configure(this.resources, project.copySpec())
            into extraResources
        }

        File osgiJar = new File(target, "osgi_${jar.file.name}")
        project.ant.zip(destfile: osgiJar) {
            zipfileset(src: jar.file, excludes: 'META-INF/MANIFEST.MF')
        }
        project.ant.zip(update: 'true', destfile: osgiJar) {
            fileset(dir: extraResources)
        }
    }

    ResolvedArtifact findJarArtifact(ResolvedDependency dependency) {
         dependency.moduleArtifacts.find { it.extension == 'jar' }
    }

    Set<String> packageNames(File jar, String filteredPackagesPattern) {
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

    String manifestFor(String manifestTemplate, Collection<String> packageNames, String mainVersion, String fullVersion) {
        StringBuilder manifest = new StringBuilder(manifestTemplate)

        if (!packageNames.isEmpty()) {
            String exportedPackages = packageNames.collect { " ${it};version=\"${mainVersion}\"" }.join(',\n')
            manifest.append "Export-Package:${exportedPackages}\n"
        }
        manifest.append "Bundle-Version: $fullVersion\n"
        manifest.toString()
    }
}
