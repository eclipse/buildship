package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory

import org.eclipse.core.resources.IFolder
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.util.file.FileUtils

class SourceFolderUpdaterTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
    }

    def "Create a source folder"() {
        given:
        def project = javaProject('name' : 'project-name', 'model-source-folders' : [], 'manual-source-folders': [])
        def newModelSourceFolders  = gradleSourceFolders(['src'])

        expect:
        project.rawClasspath.length == 0

        when:
        SourceFolderUpdater.update(project, newModelSourceFolders, null)

        then:
        project.rawClasspath.length == 1
        project.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        project.rawClasspath[0].path.toPortableString() == "/project-name/src"
    }

    def "Duplicate source folders are merged into one source entry"() {
        given:
        def project = javaProject('name' : 'project-name', 'model-source-folders' : [], 'manual-source-folders': [])
        def newModelSourceFolders  = gradleSourceFolders(['src', 'src'])

        when:
        SourceFolderUpdater.update(project, newModelSourceFolders, null)

        then:
        project.rawClasspath.length == 1
    }

    def "Previous mode source folders are removed if they no longer exist in the Gradle model"() {
        given:
        def project = javaProject('name' : 'project-name', 'model-source-folders' : ['src-old'], 'manual-source-folders': [])
        def newModelSourceFolders  = gradleSourceFolders(['src-new'])

        when:
        SourceFolderUpdater.update(project, newModelSourceFolders, null)

        then:
        project.rawClasspath.length == 1
        project.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        project.rawClasspath[0].path.toPortableString() == "/project-name/src-new"
    }

    def "Non-model source folders are preserved even if they are not part of the Gradle model" () {
        given:
        def project = javaProject('name' : 'project-name', 'model-source-folders' : [], 'manual-source-folders': ['src'])
        def newModelSourceFolders  = gradleSourceFolders(['src-gradle'])

        expect:
        project.rawClasspath.length == 1

        when:
        SourceFolderUpdater.update(project, newModelSourceFolders, null)

        then:
        project.rawClasspath.length == 2
        project.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        project.rawClasspath[0].path.toPortableString() == "/project-name/src-gradle"
        project.rawClasspath[1].entryKind == IClasspathEntry.CPE_SOURCE
        project.rawClasspath[1].path.toPortableString() == "/project-name/src"
    }

    def "Updating the sources preserve the order defined in the model"() {
        given:
        def project = javaProject('name' : 'project-name', 'model-source-folders' : [], 'manual-source-folders': [])
        def newModelSourceFolders  = gradleSourceFolders(['c', 'a', 'b'])

        when:
        SourceFolderUpdater.update(project, newModelSourceFolders, null)

        then:
        project.rawClasspath.length == 3
        project.rawClasspath[0].path.toPortableString().endsWith('c')
        project.rawClasspath[1].path.toPortableString().endsWith('a')
        project.rawClasspath[2].path.toPortableString().endsWith('b')
    }

    def "If the Gradle model contains a source folder which is was previously defined manually then the folder is transformed to a Gradle source folder" () {
        given:
        def project = javaProject('name' : 'project-name', 'model-source-folders' : [], 'manual-source-folders': ['src'])
        def newModelSourceFolders  = gradleSourceFolders(['src'])

        expect:
        project.rawClasspath.length == 1
        project.rawClasspath[0].path.toPortableString() == "/project-name/src"
        project.rawClasspath[0].extraAttributes.length == 0

        when:
        SourceFolderUpdater.update(project, newModelSourceFolders, null)

        then:
        project.rawClasspath.length == 1
        project.rawClasspath[0].path.toPortableString() == "/project-name/src"
        project.rawClasspath[0].extraAttributes.length == 1
        project.rawClasspath[0].extraAttributes[0].name == SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL
    }

    private List<OmniEclipseSourceDirectory> gradleSourceFolders(List<String> folderPaths) {
        folderPaths.collect { String folderPath ->
            def sourceDirectory = Mock(OmniEclipseSourceDirectory)
            sourceDirectory.getPath() >> folderPath
            sourceDirectory
        }
    }

    private IJavaProject javaProject(HashMap arguments) {
        def projectName = arguments['name']
        def modelSourceFolders = arguments['model-source-folders']
        def manualSourceFolders = arguments['manual-source-folders']

        // create project folder
        def location = tempFolder.newFolder(projectName)

        // create project
        def project = CorePlugin.workspaceOperations().createProject('project-name', location, [], [], new NullProgressMonitor())
        def description = project.getDescription()
        description.setNatureIds([JavaCore.NATURE_ID] as String[])
        project.setDescription(description, new NullProgressMonitor())

        // convert it to a java project
        def javaProject = JavaCore.create(project)
        IFolder outputLocation = project.getFolder('bin')
        FileUtils.ensureFolderHierarchyExists(outputLocation)
        javaProject.setOutputLocation(outputLocation.getFullPath(), new NullProgressMonitor())

        // define source classpath
        def manualSourceEntries = manualSourceFolders.collect { String path ->
            def folder = javaProject.project.getFolder(path)
            FileUtils.ensureFolderHierarchyExists(folder)
            def root = javaProject.getPackageFragmentRoot(folder)
            JavaCore.newSourceEntry(root.path)
        }
        def modelSourceEntries = modelSourceFolders.collect { String path ->
            def folder = javaProject.project.getFolder(path)
            FileUtils.ensureFolderHierarchyExists(folder)
            def root = javaProject.getPackageFragmentRoot(folder)
            def attribute = JavaCore.newClasspathAttribute(SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL, "true")
            JavaCore.newSourceEntry(root.path, new IPath[0], new IPath[0], null, [attribute] as IClasspathAttribute[])
        }
        javaProject.setRawClasspath(manualSourceEntries + modelSourceEntries as IClasspathEntry[], new NullProgressMonitor())

        // return the created instance
        javaProject
    }

}
