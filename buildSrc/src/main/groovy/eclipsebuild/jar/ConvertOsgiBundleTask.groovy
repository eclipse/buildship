package eclipsebuild.jar

import eclipsebuild.PluginUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class ConvertOsgiBundleTask extends DefaultTask {

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

    @Input
    ConfigurableFileCollection resources

    @OutputDirectory
    File target

    @TaskAction
    void convertOsgiBundle() {
        createNewBundle(project, JarBundleUtils.firstDependencyJar(pluginConfiguration))
    }

    void createNewBundle(Project project, File jar) {
        String sourceReference = PluginUtils.sourceReference(project)
        String manifest = JarBundleUtils.manifestContent(jar, template.get(), packageFilter.get(), bundleVersion.get(), qualifier.get(), sourceReference)

        File extraResources = project.file("${project.buildDir}/tmp/bundle-resources")
        File manifestFile = new File(extraResources, '/META-INF/MANIFEST.MF')
        manifestFile.parentFile.mkdirs()
        manifestFile.text = manifest

        File osgiJar = new File(target, "osgi_${jar.name}")
        project.ant.zip(destfile: osgiJar) {
            zipfileset(src: jar, excludes: 'META-INF/MANIFEST.MF')
        }

        project.ant.zip(update: 'true', destfile: osgiJar) {
            fileset(dir: extraResources)
        }

        resources.files.each { File resource ->
            project.ant.zip(update: 'true', destfile: osgiJar) {
                fileset(dir: resource)
            }
        }
    }
}
