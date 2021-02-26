package eclipsebuild.jar

import eclipsebuild.PluginUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class ConvertOsgiBundleTask extends DefaultTask {

    @Input
    abstract Property<String> getBundleName()

    @Input
    abstract Property<String> getBundleVersion()

    @Input
    abstract Property<String> getPackageFilter()

    @Input
    abstract Property<String> getQualifier()

    @Input
    abstract Property<String> getTemplate()

    @Classpath
    abstract Property<Configuration> getPluginConfiguration()

    @InputFiles
    abstract ConfigurableFileCollection getResources()

    @OutputDirectory
    abstract DirectoryProperty getOutputDirectory()

    @TaskAction
    void convertOsgiBundle() {
        createNewBundle(project, JarBundleUtils.firstDependencyJar(pluginConfiguration.get()))
    }

    void createNewBundle(Project project, File depJar) {
        String sourceReference = PluginUtils.sourceReference(project)
        Iterable<File> localJars = project.tasks['jar'].outputs.files
        String manifest = JarBundleUtils.manifestContent([depJar] + localJars, template.get(), packageFilter.get(), bundleVersion.get(), qualifier.get(), sourceReference)

        File extraResources = project.file("${project.buildDir}/tmp/bundle-resources")
        File manifestFile = new File(extraResources, '/META-INF/MANIFEST.MF')
        manifestFile.parentFile.mkdirs()
        manifestFile.text = manifest

        File osgiJar = new File(outputDirectory.get().asFile, "osgi_${project.name}.jar")
        project.ant.zip(destfile: osgiJar) {
            localJars.each { jar -> zipfileset(src: jar, excludes: 'META-INF/MANIFEST.MF')}
            zipfileset(src: depJar, excludes: 'META-INF/MANIFEST.MF')
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
