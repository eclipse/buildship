package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore

import org.eclipse.core.filesystem.EFS
import org.eclipse.core.resources.IFolder
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IResourceFilterDescription
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.util.file.FileUtils

class ResourceFilterTest extends WorkspaceSpecification {

    IProject project

    def setup() {
        project = newProject("sample-project")
    }

    def "Defining a resource filter on the project"() {
        when:
        filterChildren('filtered')

        then:
        isFiltered('filtered')
        !isFiltered('unfiltered')
    }

    def "Defining a resource filter on a subfolder"() {
        when:
        filterChildren('basefolder/subfolder')

        then:
        !isFiltered('basefolder')
        isFiltered('basefolder/subfolder')
    }

    def "Defining a resource filter on a direct child folder does not hide anything in inner folder structure"() {
        when:
        filterChildren('pkg')

        then:
        isFiltered('pkg')
        !isFiltered('src/main/java/pkg')
    }

    def "Defining a resource filter on non-child location is ignored"() {
        given:
        ResourceFilter.updateFilters(project, [dir('siblingproject')], null)

        expect:
        project.getFilters().length == 0
    }

    def "Updating resource filters removes previously defined resource filters"() {
        when:
        filterChildren('alpha')

        then:
        project.filters.length == 1
        isFiltered('alpha')

        when:
        filterChildren('beta')

        then:
        project.filters.length == 1
        isFiltered('beta')
    }

    def "Defining a new resource filter is idempotent"() {
        given:
        filterChildren('alpha')

        expect:
        project.filters.length == 1
        isFiltered('alpha')

        when:
        filterChildren('alpha')

        then:
        project.filters.length == 1
        isFiltered('alpha')
    }

    def "Removing a filter"() {
        when:
        filterChildren('filtered')
        detachFilters()

        then:
        !isFiltered('filtered')
    }

    def "Removing a filter does not modify manually created filters"() {
        given:
        filterChildren('filtered')
        int type = IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE
        def matchers = ResourceFilter.createMatchers(project, [project.getFolder('manuallyfiltered').getLocation().toFile()] as List)
        project.createFilter(type, matchers[0], IResource.NONE, null)

        when:
        detachFilters()

        then:
        isFiltered('manuallyfiltered')
        !isFiltered('filtered')
    }

    def "Can add zero filters"() {
        ResourceFilter.updateFilters(project, [], null)

        expect:
        project.filters.length == 0
    }

    def "Can add a large number of filters" () {
        given:
        String[] filterNames = (1..500).collect { 'filtered' + it }
        filterChildren(filterNames)

        expect:
        filterNames.each {
            assert isFiltered(it)
        }
    }

    private void filterChildren(String... filteredFolderNames) {
        def filteredFolders = filteredFolderNames.collect { new File(project.location.toFile(), it) }
        ResourceFilter.updateFilters(project, filteredFolders, null)
    }

    private detachFilters() {
        ResourceFilter.detachAllFilters(project, null)
    }

    private boolean isFiltered(String filteredFolderName) {
        !workspace.validateFiltered(project.getFolder(filteredFolderName)).isOK()
    }

}
