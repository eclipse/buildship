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

class ProcessOsgiBundleTask extends SingleDependencyProjectTask {

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
        createNewBundle(dependencyJar)
    }

    void createNewBundle(File jar) {
        List<String> packageNames = packageNames(jar, this.packageFilter.get()) as List
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

        File osgiJar = new File(target, "osgi_${jar.name}")
        project.ant.zip(destfile: osgiJar) {
            zipfileset(src: jar, excludes: 'META-INF/MANIFEST.MF')
        }
        project.ant.zip(update: 'true', destfile: osgiJar) {
            fileset(dir: extraResources)
        }
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
