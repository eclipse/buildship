package eclipsebuild.jar

import java.util.Set
import java.util.regex.Matcher
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction


class ExistingJarBundleEclipseProjectTask extends DefaultTask {

    @Input
    Property<String> bundleVersion

    @Input
    Property<String> qualifier

    @Input
    Configuration pluginConfiguration

    // TODO (donat) merge duplicate code found in ProcessOsgiBundleTask

    Set<ResolvedDependency> pluginDependencies() {
        pluginConfiguration.resolvedConfiguration.firstLevelModuleDependencies
    }

    ResolvedArtifact findJarArtifact(ResolvedDependency dependency) {
        dependency.moduleArtifacts.find { it.extension == 'jar' }
   }

    @TaskAction
    void generateBuildshipProject() {
        File jarFile = findJarArtifact(pluginDependencies()[0]).file

        // create manifest and place it in the META-INF folder
        def manifestFile = new File(project.projectDir, "META-INF/MANIFEST.MF")
        if (!manifestFile.parentFile.exists()) {
            def success = manifestFile.parentFile.mkdirs()
            if (!success) {
                throw new GradleException("Unable to create directory ${manifestFile.parentFile.absolutePath}")
            }
        }
        manifestFile.text = calculateManifest(project.version, jarFile)

        // copy the jar to the project location
        project.copy {
            from jarFile
            into project.projectDir
        }

        // create the .project and .classpath files
        new File(project.projectDir, '.project').text = getDotProjectText()
        new File(project.projectDir, '.classpath').text = getDotClasspathText(jarFile.name)
    }

    private String calculateManifest(String projectVersion, File jar) {
        def packageNames = getPackageNames(jar)
        def packageExports = getPackageExports(packageNames, bundleVersion.get())
     """Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Gradle Tooling API
Bundle-Vendor: Gradle Inc.
Bundle-SymbolicName: org.gradle.toolingapi
Bundle-Version: ${bundleVersion.get()}.v${qualifier.get()}
Bundle-ClassPath: ${jar.name}
Bundle-RequiredExecutionEnvironment: JavaSE-1.6
Export-Package: ${packageExports}
Require-Bundle: org.slf4j.api;bundle-version="1.7.2"
"""
}

    private Set<String> getPackageNames(File jar) {
        def result = [] as Set
        new ZipInputStream(new FileInputStream(jar)).withCloseable { zip ->
            ZipEntry e
            while (e = zip.nextEntry) {
                if (!e.directory && e.name.endsWith(".class")) {
                    int index = e.name.lastIndexOf('/')
                    if (index < 0) index = e.name.length()
                    String packageName = e.name.substring(0, index).replace('/', '.')
                    result.add(packageName)
                }
            }
        }
        result
    }

    private String getPackageExports(Set<String> packageNames, String bundleVersion) {
        // the Tooling API has more than two packages
        StringBuilder exportedPackages = new StringBuilder("${packageNames[0]};version=\"${bundleVersion}\"")
        for (i in 1..< packageNames.size()) {
            exportedPackages.append ",\n ${packageNames[i]};version=\"${bundleVersion}\""
        }
        exportedPackages.toString()
    }

    private String getDotProjectText() {
        """<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
  <name>org.gradle.toolingapi</name><comment/><projects/>
  <buildSpec>
    <buildCommand><name>org.eclipse.jdt.core.javabuilder</name><arguments/></buildCommand>
    <buildCommand><name>org.eclipse.pde.ManifestBuilder</name><arguments/></buildCommand>
    <buildCommand><name>org.eclipse.pde.SchemaBuilder</name><arguments/></buildCommand>
  </buildSpec>
  <natures>
    <nature>org.eclipse.pde.PluginNature</nature>
    <nature>org.eclipse.jdt.core.javanature</nature>
  </natures>
</projectDescription>
"""
    }

    private String getDotClasspathText(String jarName) {
        """<?xml version="1.0" encoding="UTF-8"?>
<classpath>
  <classpathentry exported="true" kind="lib" path="${jarName}"/>
  <classpathentry kind="output" path="bin"/>
</classpath>
"""
    }


}
