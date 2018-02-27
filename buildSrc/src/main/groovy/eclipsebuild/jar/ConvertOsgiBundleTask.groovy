package eclipsebuild.jar

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ConfigureUtil

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

    Closure resources = {}

    @OutputDirectory
    File target

    @TaskAction
    void convertOsgiBundle() {
        createNewBundle(JarBundleUtils.firstDependencyJar(pluginConfiguration))
    }

    void createNewBundle(File jar) {
        String manifest = JarBundleUtils.manifestContent(jar, template.get(), packageFilter.get(), bundleVersion.get(), qualifier.get())

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
}
