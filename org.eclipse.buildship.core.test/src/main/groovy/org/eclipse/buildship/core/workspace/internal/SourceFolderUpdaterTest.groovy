package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import org.eclipse.core.resources.IFolder
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute
import org.eclipse.buildship.core.omnimodel.OmniEclipseSourceDirectory
import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.util.file.FileUtils
import org.eclipse.buildship.core.util.gradle.Maybe

class SourceFolderUpdaterTest extends WorkspaceSpecification {

    IJavaProject javaProject

    def setup() {
        javaProject = newJavaProject("project-name")
        javaProject.setRawClasspath([] as IClasspathEntry[], new NullProgressMonitor())
    }

    def "Model source folders are added"() {
        given:
        def newModelSourceFolders = gradleSourceFolders(['src'])

        expect:
        javaProject.rawClasspath.length == 0

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src"

    }

    def "Duplicate model source folders are merged into one source entry"() {
        given:
        def newModelSourceFolders = gradleSourceFolders(['src', 'src'])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src"
    }

    def "Source folders that don't physically exist are ignored."() {
        given:
        def newModelSourceFolders = gradleSourceFolders(['src-not-there'])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 0
    }

    def "Previous source folders are removed if they no longer exist in the Gradle model"() {
        given:
        addSourceFolder("src-old", [], [], null)
        def srcNew = javaProject.project.getFolder('src-new')
        srcNew.create(true, true, null)
        def newModelSourceFolders = gradleSourceFolders([srcNew.name])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src-new"
    }

    def "Manually added source folders are removed if they are not part of the Gradle model"() {
        given:
        addSourceFolder("src")
        def srcGradle = javaProject.project.getFolder('src-gradle')
        srcGradle.create(true, true, null)
        def newModelSourceFolders = gradleSourceFolders([srcGradle.name])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src-gradle"
    }

    def "User-defined attributes are kept if not present in the Gradle model"() {
        given:
        addSourceFolder("src", ['manual-inclusion-pattern'], ['manual-exclusion-pattern'], 'foo', attributes(["foo" : "bar"]))
        def newModelSourceFolders = gradleSourceFolders(['src'])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        IClasspathEntry entryAfterUpdate = javaProject.rawClasspath[0]
        entryAfterUpdate.getInclusionPatterns()[0].toString() == "manual-inclusion-pattern"
        entryAfterUpdate.getExclusionPatterns()[0].toString() == "manual-exclusion-pattern"
        entryAfterUpdate.extraAttributes as List == attributes(["foo" : "bar"]) as List
        entryAfterUpdate.outputLocation.toString() == "/project-name/foo"
    }

    def "User-defined attributes are overwritten if present in the Gradle model"() {
        given:
        addSourceFolder("src", ['manual-inclusion-pattern'], ['manual-exclusion-pattern'], 'foo', attributes(["foo" : "bar"]))
        def newModelSourceFolders = gradleSourceFolders(['src'], Optional.of(['model-excludes']),
            Optional.of(['model-includes']), Optional.of(['model-key' : 'model-value']), Maybe.of('model-output'))

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        IClasspathEntry entryAfterUpdate = javaProject.rawClasspath[0]
        entryAfterUpdate.exclusionPatterns.collect { it.toPortableString() }  == ['model-excludes']
        entryAfterUpdate.inclusionPatterns.collect { it.toPortableString() }  == ['model-includes']
        entryAfterUpdate.extraAttributes as List == attributes(['model-key' : 'model-value']) as List
        entryAfterUpdate.outputLocation.toString() == '/project-name/model-output'
    }

    def "The project root can be a source folder"() {
        given:
        def newModelSourceFolders = gradleSourceFolders(['.'])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name"
    }

    def "Can configure exclude and include patterns"() {
        given:
        def sourceFolders = gradleSourceFolders(['src'], Optional.of(['java/**']), Optional.of(['**/Test*']))

        expect:
        javaProject.rawClasspath.length == 0

        when:
        SourceFolderUpdater.update(javaProject, sourceFolders, null)

        then:
        javaProject.rawClasspath[0].inclusionPatterns.length == 1
        javaProject.rawClasspath[0].inclusionPatterns[0].toPortableString() == '**/Test*'
        javaProject.rawClasspath[0].exclusionPatterns.length == 1
        javaProject.rawClasspath[0].exclusionPatterns[0].toPortableString() == 'java/**'
    }

    def "Can configure extra attributes"() {
        given:
        def sourceFolders = gradleSourceFolders(['src'], Optional.absent(), Optional.absent(), Optional.of(['customKey': 'customValue']))

        when:
        SourceFolderUpdater.update(javaProject, sourceFolders, null)

        then:
        javaProject.rawClasspath[0].extraAttributes as List == attributes(['customKey': 'customValue']) as List
    }

    def "Can configure custom output location"() {
        given:
        def sourceFolders = gradleSourceFolders(['src'], Optional.absent(), Optional.absent(), Optional.absent(), modelLocation)

        when:
        SourceFolderUpdater.update(javaProject, sourceFolders, null)

        then:
        javaProject.rawClasspath[0].outputLocation?.toPortableString() == expectedLocation

        where:
        modelLocation              | expectedLocation
        Maybe.absent()             | null
        Maybe.of(null)             | null
        Maybe.of('target/classes') | '/project-name/target/classes'

    }

    private List<OmniEclipseSourceDirectory> gradleSourceFolders(List<String> folderPaths, Optional excludes = Optional.absent(),
                                                                 Optional includes = Optional.absent(), Optional attributes = Optional.absent(),
                                                                 Maybe<String> output = Maybe.absent()) {
        folderPaths.collect { String folderPath ->
            OmniEclipseSourceDirectory sourceDirectory = Mock(OmniEclipseSourceDirectory)
            sourceDirectory.getPath() >> folderPath
            sourceDirectory.getClasspathAttributes() >> gradleClasspathAttributes(attributes)
            sourceDirectory.getExcludes() >> excludes
            sourceDirectory.getIncludes() >> includes
            sourceDirectory.getOutput() >> output
            sourceDirectory
        }
    }

    private Optional<List<OmniClasspathAttribute>> gradleClasspathAttributes(Optional attributes) {
        if (!attributes.present) {
            return Optional.absent()
        } else {
            def result = attributes.get().collect { k, v ->
                OmniClasspathAttribute attribute = Mock(OmniClasspathAttribute)
                attribute.getName() >> k
                attribute.getValue() >> v
                attribute
            }
            return Optional.of(result)
        }
    }

    private void addSourceFolder(String path, List<String> inclusionPatterns = [], List<String> exclusionPatterns = [], String outputLocation = null, IClasspathAttribute[] extraAttributes = []) {
        IFolder folder = javaProject.project.getFolder(path)
        IPath fullOutputPath = outputLocation == null ? null : javaProject.project.getFolder(outputLocation).fullPath
        FileUtils.ensureFolderHierarchyExists(folder)
        def root = javaProject.getPackageFragmentRoot(folder)
        def entry = JavaCore.newSourceEntry(
            root.path,
            paths(inclusionPatterns),
            paths(exclusionPatterns),
            fullOutputPath,
            extraAttributes
        )
        javaProject.setRawClasspath((javaProject.rawClasspath + entry) as IClasspathEntry[], new NullProgressMonitor())
    }

    private IPath[] paths(List<String> patterns) {
        patterns.collect { new Path(it) } as Path[]
    }

    private IClasspathAttribute[] attributes(Map<String, String> attributes) {
        return attributes.entrySet().collect {
            attribute(it.key, it.value)
        }
    }

    private IClasspathAttribute attribute(String key, String value) {
        JavaCore.newClasspathAttribute(key, value)
    }
}
