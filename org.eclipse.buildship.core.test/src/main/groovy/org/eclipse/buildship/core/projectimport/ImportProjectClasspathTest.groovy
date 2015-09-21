package org.eclipse.buildship.core.projectimport

import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject

class ImportProjectClasspathTest extends ProjectImportSpecification {

    def "Projectimport without classpath file but existing project file, creates a classpath file"() {
        setup:
        def location = createSampleProject()
        file('app', '.project') << '''
<projectDescription>
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
        <filter>
            <id>1442824222615</id>
            <name></name>
            <type>26</type>
            <matcher>
                <id>org.eclipse.ui.ide.multiFilter</id>
                <arguments>1.0-projectRelativePath-matches-false-false-build</arguments>
            </matcher>
        </filter>
        <filter>
            <id>1442824222630</id>
            <name></name>
            <type>26</type>
            <matcher>
                <id>org.eclipse.ui.ide.multiFilter</id>
                <arguments>1.0-projectRelativePath-matches-false-false-.gradle</arguments>
            </matcher>
        </filter>
    </filteredResources>
</projectDescription> '''

        when:
        executeProjectImportAndWait(location)

        then:
        IProject customProject = findProject('classpath')
        IFile classpathFile = customProject.getFile('.classpath')
        classpathFile != null
        classpathFile.exists() != null
    }

    def "Projectimport does not override existing classpath file contents"() {
        setup:
        def location = createSampleProject()
        file('app', '.project') << '''<projectDescription>
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
        <filter>
            <id>1442824222615</id>
            <name></name>
            <type>26</type>
            <matcher>
                <id>org.eclipse.ui.ide.multiFilter</id>
                <arguments>1.0-projectRelativePath-matches-false-false-build</arguments>
            </matcher>
        </filter>
        <filter>
            <id>1442824222630</id>
            <name></name>
            <type>26</type>
            <matcher>
                <id>org.eclipse.ui.ide.multiFilter</id>
                <arguments>1.0-projectRelativePath-matches-false-false-.gradle</arguments>
            </matcher>
        </filter>
    </filteredResources>
</projectDescription>'''

        file('app', '.classpath') <<
                '''<classpath>
    <classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
    <classpathentry kind="con" path="existing-classpath-file-entry"/>
    <classpathentry kind="output" path="bin"/>
</classpath>'''

        when:
        executeProjectImportAndWait(location)

        IProject customProject = findProject('classpath')
        IFile classpathFile = customProject.getFile('.classpath')

        then:
        classpathFile != null
        def classPathFile = classpathFile.getLocation().toFile()
        classPathFile.exists()
        classPathFile.text.contains('existing-classpath-file-entry')
    }

    private def createSampleProject() {
        def location = folder('app')
        folder('app', 'src' , 'main', 'java')
        folder('app', 'src' , 'test', 'java')
        file('app', 'settings.gradle') << 'rootProject.name = \'classpath\''
        file('app', 'build.gradle') << '''
        apply plugin: 'java'

        repositories {
            jcenter()
        }

        dependencies {
            compile 'org.slf4j:slf4j-api:1.7.12'

            testCompile 'junit:junit:4.12'
        }'''

        location
    }
}
