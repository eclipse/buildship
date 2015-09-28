package org.eclipse.buildship.core.projectimport

import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject

class ImportProjectClasspathTest extends ProjectImportSpecification {

    def "If imported project has .project file but no .classpath file, then the .classpath file is created"() {
        setup:
        def location = createSampleProject()
        file('app', '.project') <<
        '''<projectDescription>
             <name>classpath</name>
             <comment>Project Classpath created by Buildship.</comment>
             <projects>
             </projects>
             <buildSpec>
               <buildCommand>
                 <name>org.eclipse.buildship.core.gradleprojectbuilder</name>
                 <arguments>
                 </arguments>
               </buildCommand>
             </buildSpec>
             <natures>
               <nature>org.eclipse.buildship.core.gradleprojectnature</nature>
             </natures>
             <filteredResources>
             </filteredResources>
           </projectDescription>
        '''

        when:
        executeProjectImportAndWait(location)

        then:
        IProject customProject = findProject('classpath')
        customProject.getFile('.classpath').exists()
    }

    def "Importing a project does not override existing .classpath file"() {
        setup:
        def location = createSampleProject()
        file('app', '.project') <<
        '''<projectDescription>
             <name>classpath</name>
             <comment>Project Classpath created by Buildship.</comment>
             <projects>
             </projects>
             <buildSpec>
               <buildCommand>
                 <name>org.eclipse.jdt.core.javabuilder</name>
                   <arguments>
                   </arguments>
               </buildCommand>
               <buildCommand>
                 <name>org.eclipse.buildship.core.gradleprojectbuilder</name>
                   <arguments>
                   </arguments>
               </buildCommand>
             </buildSpec>
             <natures>
               <nature>org.eclipse.buildship.core.gradleprojectnature</nature>
               <nature>org.eclipse.jdt.core.javanature</nature>
             </natures>
             <filteredResources>
             </filteredResources>
        </projectDescription>
        '''

        file('app', '.classpath') <<
        '''<classpath>
             <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
             <classpathentry kind="lib" path="lib/existing-dependency.jar"/>
             <classpathentry kind="output" path="bin"/>
           </classpath>
        '''

        when:
        executeProjectImportAndWait(location)

        then:
        File classpathFile = findProject('classpath').getFile('.classpath').location.toFile()
        classpathFile.exists()

        classpathFile.text.contains('existing-dependency.jar')
    }

    private def createSampleProject() {
        def location = folder('app')
        folder('app', 'src' , 'main', 'java')
        folder('app', 'src' , 'test', 'java')
        folder('app', 'src', 'main', 'groovy')
        file('app', 'settings.gradle') << 'rootProject.name = "classpath"'
        file('app', 'build.gradle') <<
        '''apply plugin: 'java'
           repositories { jcenter() }
           dependencies {
               compile 'org.slf4j:slf4j-api:1.7.12'
               testCompile 'junit:junit:4.12'
           }
        '''
        location
    }
}
