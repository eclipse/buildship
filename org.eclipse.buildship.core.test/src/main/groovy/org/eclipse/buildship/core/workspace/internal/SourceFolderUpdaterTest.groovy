package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Optional

import com.gradleware.tooling.toolingmodel.OmniClasspathAttribute
import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory
import com.gradleware.tooling.toolingmodel.util.Maybe

import org.eclipse.core.resources.IFolder
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.util.file.FileUtils

class SourceFolderUpdaterTest extends WorkspaceSpecification {

    IJavaProject javaProject

    void setup() {
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
        javaProject.rawClasspath[0].extraAttributes.length == 1
        javaProject.rawClasspath[0].extraAttributes[0].name == SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL

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
        javaProject.rawClasspath[0].extraAttributes.length == 1
        javaProject.rawClasspath[0].extraAttributes[0].name == SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL
    }

    def "Source folders that don't physically exist are ignored."() {
        given:
        def newModelSourceFolders = gradleSourceFolders(['src-not-there'])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 0
    }

    def "Previous model source folders are removed if they no longer exist in the Gradle model"() {
        given:
        addSourceFolder("src-old", [], [], null, attributes(fromGradleModel()))
        def srcNew = javaProject.project.getFolder('src-new')
        srcNew.create(true, true, null)
        def newModelSourceFolders = gradleSourceFolders([srcNew.name])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src-new"
        javaProject.rawClasspath[0].extraAttributes.length == 1
        javaProject.rawClasspath[0].extraAttributes[0].name == SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL
    }

    def "Non-model source folders are preserved even if they are not part of the Gradle model"() {
        given:
        addSourceFolder("src")
        def srcGradle = javaProject.project.getFolder('src-gradle')
        srcGradle.create(true, true, null)
        def newModelSourceFolders = gradleSourceFolders([srcGradle.name])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 2
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src-gradle"
        javaProject.rawClasspath[0].extraAttributes.length == 1
        javaProject.rawClasspath[0].extraAttributes[0].name == SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL
        javaProject.rawClasspath[1].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[1].path.toPortableString() == "/project-name/src"
        javaProject.rawClasspath[1].extraAttributes.length == 0
    }

    def "Model source folders that were previously defined manually are transformed to model source folders"() {
        given:
        addSourceFolder("src")
        def newModelSourceFolders = gradleSourceFolders(['src'])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        javaProject.rawClasspath.length == 1
        javaProject.rawClasspath[0].entryKind == IClasspathEntry.CPE_SOURCE
        javaProject.rawClasspath[0].path.toPortableString() == "/project-name/src"
        javaProject.rawClasspath[0].extraAttributes.length == 1
        javaProject.rawClasspath[0].extraAttributes[0].name == SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL
    }

    def "When becoming part of model, unsupported source folder attributes are preserved"() {
        given:
        addSourceFolder("src", ['manual-inclusion-pattern'], ['manual-exclusion-pattern'], 'foo', attributes(["foo" : "bar"]))
        def newModelSourceFolders = gradleSourceFolders(['src'])

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)

        then:
        IClasspathEntry entryAfterUpdate = javaProject.rawClasspath[0]
        entryAfterUpdate.getInclusionPatterns()[0].toString() == "manual-inclusion-pattern"
        entryAfterUpdate.getExclusionPatterns()[0].toString() == "manual-exclusion-pattern"
        entryAfterUpdate.extraAttributes as List == attributes(["foo" : "bar"] << fromGradleModel()) as List
        entryAfterUpdate.outputLocation.toString() == "/project-name/foo"
    }

    def "When becoming part of model, supported source folder attributes are overridden"() {
        given:
        addSourceFolder("src", ['manual-inclusion-pattern'], ['manual-exclusion-pattern'], 'foo', attributes(["foo" : "bar"]))
        def newModelSourceFolders = gradleSourceFolders(['src'], Optional.of(['model-excludes']),
            Optional.of(['model-includes']), Optional.of(['model-key' : 'model-value']), Maybe.of('model-output'))

        when:
        SourceFolderUpdater.update(javaProject, newModelSourceFolders, null)
        javaProject.rawClasspath.each {println it }

        then:
        IClasspathEntry entryAfterUpdate = javaProject.rawClasspath[0]
        entryAfterUpdate.exclusionPatterns.collect { it.toPortableString() }  == ['model-excludes']
        entryAfterUpdate.inclusionPatterns.collect { it.toPortableString() }  == ['model-includes']
        entryAfterUpdate.extraAttributes as List == attributes(['model-key' : 'model-value'] << fromGradleModel()) as List
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
        javaProject.rawClasspath[0].extraAttributes.length == 1
        javaProject.rawClasspath[0].extraAttributes[0].name == SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL
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
        javaProject.rawClasspath[0].extraAttributes as List == attributes(['customKey': 'customValue'] << fromGradleModel()) as List
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

    private Map<String, String> fromGradleModel() {
        [(SourceFolderUpdater.CLASSPATH_ATTRIBUTE_FROM_GRADLE_MODEL) : "true"]
    }

    private IClasspathAttribute attribute(String key, String value) {
        JavaCore.newClasspathAttribute(key, value)
    }
}
