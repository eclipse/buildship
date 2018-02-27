package eclipsebuild.jar

import java.util.regex.Pattern
import java.util.zip.*

import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.util.ConfigureUtil

class ProcessOsgiBundleTask extends BaseJarBundleTask {

    Closure resources = {}

    @OutputDirectory
    File target

    @TaskAction
    void processOsgiBundles() {
        createNewBundle(dependencyJar)
    }

    void createNewBundle(File jar) {
        String manifest = manifestContent(jar)

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
