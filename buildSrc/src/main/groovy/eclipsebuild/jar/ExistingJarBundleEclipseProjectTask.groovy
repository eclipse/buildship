package eclipsebuild.jar

import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class ExistingJarBundleEclipseProjectTask extends BaseJarBundleTask {

    @TaskAction
    void generateEclipseProject() {
        File jarFile = dependencyJar

        // create manifest and place it in the META-INF folder
        def manifestFile = new File(project.projectDir, "META-INF/MANIFEST.MF")
        if (!manifestFile.parentFile.exists()) {
            def success = manifestFile.parentFile.mkdirs()
            if (!success) {
                throw new GradleException("Unable to create directory ${manifestFile.parentFile.absolutePath}")
            }
        }
        manifestFile.text = manifestContent(jarFile)

        // copy the jar to the project location
        project.copy {
            from jarFile
            into project.projectDir
        }

        // create the .project and .classpath files
        new File(project.projectDir, '.project').text = getDotProjectText()
        new File(project.projectDir, '.classpath').text = getDotClasspathText(jarFile.name)
    }

    private String getDotProjectText() {
        """<?xml version="1.0" encoding="UTF-8"?>
<projectDescription>
  <name>${bundleName.get()}</name><comment/><projects/>
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
