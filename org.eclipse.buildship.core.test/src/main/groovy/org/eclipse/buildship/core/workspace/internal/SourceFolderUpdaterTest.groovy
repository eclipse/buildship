package org.eclipse.buildship.core.workspace.internal

import com.gradleware.tooling.toolingmodel.OmniEclipseSourceDirectory

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathAttribute
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.util.IClassFileAttribute;

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

    def "Manual adjustments on model source folders are preserved"() {
        given:
        addSourceFolder("src", ['manual-inclusion-pattern'], ['manual-exclusion-pattern'], "foo", attributes(["foo" : "bar"] << fromGradleModel()))
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

    def "Manual adjustments on non-model source folders are preserved when becoming model source folders"() {
        given:
        addSourceFolder("src", ['manual-inclusion-pattern'], ['manual-exclusion-pattern'], "foo", attributes(["foo" : "bar"]))
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

    private List<OmniEclipseSourceDirectory> gradleSourceFolders(List<String> folderPaths) {
        folderPaths.collect { String folderPath ->
            def sourceDirectory = Mock(OmniEclipseSourceDirectory)
            sourceDirectory.getPath() >> folderPath
            sourceDirectory
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
