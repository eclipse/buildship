package org.eclipse.buildship.core.workspace.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import org.eclipse.core.filesystem.EFS
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.core.runtime.SubProgressMonitor

import org.eclipse.buildship.core.CorePlugin

class ResourceFilterTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    IProject project

    def setup() {
        // create an empty sample project for all tests
        File projectFolder = tempFolder.newFolder("project")
        IProjectDescription projectDescription = workspace().newProjectDescription("project")
        Path locationPath = Path.fromOSString(projectFolder.getPath())
        project = workspace().getRoot().getProject("project")
        project.create(projectDescription, new NullProgressMonitor())
        project.open(new NullProgressMonitor())
    }

    def cleanup() {
        project.delete(true, null)
        CorePlugin.workspaceOperations().deleteAllProjects(null)
    }

    def "Can define resource filters on the project"() {
        given:
        projectFolder('filtered')
        projectFolder('unfiltered')

        expect:
        project.getFolder('filtered').exists()
        project.getFolder('unfiltered').exists()
        workspace().validateFiltered(project.getFolder('filtered')).isOK()
        workspace().validateFiltered(project.getFolder('unfiltered')).isOK()

        when:
        ResourceFilter.attachFilters(project, [ toFile(project.getFolder('filtered')) ], null)

        then:
        !workspace().validateFiltered(project.getFolder('filtered')).isOK()
        workspace().validateFiltered(project.getFolder('unfiltered')).isOK()
    }

    def "Resource filter can be defined on subfolders"() {
        given:
        projectFolder('basefolder/subfolder')

        expect:
        project.getFolder('basefolder/subfolder').exists()
        workspace().validateFiltered(project.getFolder('basefolder/subfolder')).isOK()

        when:
        ResourceFilter.attachFilters(project, [ toFile(project.getFolder('basefolder/subfolder')) ], null)

        then:
        workspace().validateFiltered(project.getFolder('basefolder')).isOK()
        !workspace().validateFiltered(project.getFolder('basefolder/subfolder')).isOK()
    }

    def "Non-child locations are ignored"() {
        given:
        ResourceFilter.attachFilters(project, [tempFolder.newFolder('siblingproject')], null)

        expect:
        project.getFilters().length == 0
    }

    def "Resource filter on direct child folder doesn't hide anything in inner folder structure"() {
        given:
        projectFolder('pkg')
        projectFolder('src/main/java/pkg')

        when:
        ResourceFilter.attachFilters(project, [toFile(project.getFolder('pkg'))], null)

        then:
        !workspace().validateFiltered(project.getFolder('pkg')).isOK()
        workspace().validateFiltered(project.getFolder('src/main/java/pkg')).isOK()
    }

    def "Defining new resource filter preserves the previously defined ones"() {
        given:
        projectFolder('alpha')
        projectFolder('beta')

        when:
        ResourceFilter.attachFilters(project, [toFile(project.getFolder('alpha'))], null)

        then:
        project.getFilters().length == 1
        (project.getFilters()[0].getFileInfoMatcherDescription().getArguments() as String).endsWith('alpha')

        when:
        ResourceFilter.attachFilters(project, [toFile(project.getFolder('beta'))], null)

        then:
        project.getFilters().length == 2
        (project.getFilters()[0].getFileInfoMatcherDescription().getArguments() as String).endsWith('alpha')
        (project.getFilters()[1].getFileInfoMatcherDescription().getArguments() as String).endsWith('beta')
    }

    def "Resource filter definition is idempotent"() {
        given:
        projectFolder('alpha')

        expect:
        project.getFilters().length == 0

        when:
        ResourceFilter.attachFilters(project, [toFile(project.getFolder('alpha'))], null)

        then:
        project.getFilters().length == 1
        (project.getFilters()[0].getFileInfoMatcherDescription().getArguments() as String).endsWith('alpha')

        when:
        ResourceFilter.attachFilters(project, [toFile(project.getFolder('alpha'))], null)

        then:
        project.getFilters().length == 1
        (project.getFilters()[0].getFileInfoMatcherDescription().getArguments() as String).endsWith('alpha')
    }

    private static IWorkspace workspace() {
        LegacyEclipseSpockTestHelper.getWorkspace()
    }

    private def projectFolder(String path) {
        createFolder(project.getFolder(path))
    }

    private static def createFolder(IFolder folder) {
        if (!folder.exists()) {
            def parent = folder.getParent()
            if (parent instanceof IFolder) {
                createFolder((IFolder) folder.getParent())
            }
            folder.create(false, false, null)
        }
    }

    private static def toFile(IFolder folder) {
        def uri = folder.getLocationURI()
        EFS.getStore(uri).toLocalFile(0, new NullProgressMonitor())
    }
}
