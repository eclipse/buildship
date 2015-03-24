/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild
import java.util.List;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;
import org.mozilla.classfile.SuperBlock;

import aQute.bnd.maven.support.Maven;

/**
 * Gradle plugin for building Eclipse bundles
 * <p>
 * Applies the java plugin by default.
 * <p>
 * Makes the os, ws and arch variables available for the project (via project.ext) for building against platform-dependent
 * dependencies; for example <pre> compile "eclipse:org.eclipse.swt.${project.ext.ws}.${project.ext.os}.${project.ext.arch}:+"</pre>
 * <p>
 * To make the compilation work, the compileJava task depends on the establishment of the target platform.
 * <p>
 * To construct the output jar just like Eclipse PDE the plugin loads the contents of the <code>build.properties</code>
 * file and brings it in sync with the jar's content.
 * <p>
 * The <it>updateLibs</it> assigns takes all project dependencies defined in the <code>bundled</code> scope, and copies
 * them (including transitives) into the lib folder and modifies the bundle manifest to export all packages from the libraries.
 */
class BundlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        configureProject(project)
        addUpdateLibsTasks(project)
    }

    void configureProject(Project project) {
        // apply the java plugin
        project.plugins.apply('java')

        // make new variables for the build.gradle file e.g. for platform-dependent dependencies
        project.ext.ECLIPSE_OS = Constants.os;
        project.ext.ECLIPSE_WS = Constants.ws;
        project.ext.ECLIPSE_ARCH = Constants.arch;
        project.ext.ECLIPSE_VERSION = Config.on(project).eclipseVersion

        // add new configuration scope
        project.configurations.create('bundled')
        project.configurations.create('bundledSource')
        project.configurations { compile.extendsFrom bundled }

        // make the most basic task depend on the target platform assembling
        project.tasks.compileJava.dependsOn  ":installTargetPlatform"

        // Make sure the manifest ifile exists
        assert project.file('build.properties').exists()

        // Use the same MANIFEST.MF file as it is in the project except the Bundle-Version
        PluginUtils.updatePluginManifest(project)

        // Parse build.properties and sync it with output jar
        PluginUtils.syncJarContentWithBuildProperties(project)

        // assemble task does't change anything outside the buildDir folder
        project.tasks.assemble.outputs.dir project.buildDir
    }

    void addUpdateLibsTasks(Project project) {
        project.task('updateLibs', dependsOn: ['copyLibs', 'updateManifest']){
            group = Constants.gradleTaskGroupName
            description = '''Copies the bundled dependencies into the project's lib folder and updates the manifest file'''
        }

        project.task('copyLibs',  dependsOn: [
            project.configurations.bundled,
            project.configurations.bundledSource,
            ":installTargetPlatform"
        ], type: Copy) {
            group = Constants.gradleTaskGroupName
            description = 'Copies the bundled dependencies into the lib folder'

            def libDir = project.file('lib')

            // before the update delete all the libraries that are currently in the lib folder
            doFirst {
                libDir.listFiles().each { File f ->
                    if (f.toString().endsWith('.jar')) {
                        logger.info("Deleting ${f.name}")
                        f.delete()
                    }
                }
            }

            // copy the dependencies to the 'libs' folder
            into libDir
            from project.configurations.bundled
            from project.configurations.bundledSource
        }

        project.task('updateManifest', dependsOn: project.configurations.bundled) {
            group = Constants.gradleTaskGroupName
            description = 'Updates the manifest file with all the dependencies of the Tooling API'

            // check the existence of the manifest file in configuration-time
            assert project.file('META-INF/MANIFEST.MF').exists()

            doLast {
                // don't write anything if there is no bundled dependency
                if (project.configurations.bundled.dependencies.isEmpty()) {
                    return
                }

                File manifest = project.file('META-INF/MANIFEST.MF')
                List<String> lines = manifest.readLines()
                int i = 0;

                manifest.withPrintWriter { out ->
                    // copy file upto line with 'Bundle-ClassPath: .'
                    while (i < lines.size() && !lines[i].startsWith('Bundle-ClassPath: .,')) {
                        out.println(lines[i])
                        i++
                    }

                    out.print 'Bundle-ClassPath: .,'

                    // add a sorted list of jar file names under the Bundle-Classpath section
                    boolean comma = false
                    def bundledConfig = project.configurations.bundled as List
                    bundledConfig.sort { it.name }.each { File jarFile ->
                        if (jarFile.toString().endsWith('.jar')) {
                            if (comma) {
                                out.println(',')
                            } else {
                                out.println()
                            }
                            String name = jarFile.getName()
                            out.print(" lib/$name")
                            comma = true
                        }
                    }
                    out.println()

                    // skip lines up to 'Export-Package: '
                    while (i < lines.size() && !lines[i].startsWith('Export-Package: ')) {
                        i++
                    }

                    // copy the remaining lines
                    while (i < lines.size()) {
                        out.println lines[i]
                        i++
                    }
                }

                // update the .classpath file
                def classpathFile = project.file('.classpath')
                def classpathXml = new XmlParser().parse(classpathFile)
                // delete all nodes pointing to the lib folder
                classpathXml.findAll { it.name().equals('classpathentry') && it.@path.startsWith('lib/') }.each { classpathXml.remove(it) }
                // re-create the deleted nodes with the 'sourcepath' attribute
                project.configurations.bundled.sort { it.name }.each { File jarFile ->
                    def name = jarFile.getName()
                    def nameWithoutExtension = name.substring(0, name.lastIndexOf('.'))
                    new Node(classpathXml, 'classpathentry', ['exported' : 'true', 'kind' : 'lib',  'path' : "lib/$name", 'sourcepath' : "lib/${nameWithoutExtension}-sources.jar"])
                }
                new XmlNodePrinter(new PrintWriter(new FileWriter(classpathFile))).print(classpathXml)
            }

        }
    }
}
