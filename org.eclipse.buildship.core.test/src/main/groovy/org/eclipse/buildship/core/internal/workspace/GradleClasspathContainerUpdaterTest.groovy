/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import org.gradle.api.JavaVersion
import org.gradle.tooling.model.eclipse.EclipseExternalDependency
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.tooling.model.eclipse.EclipseProjectDependency
import spock.lang.IgnoreIf

import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.internal.test.fixtures.WorkspaceSpecification
import org.eclipse.buildship.core.internal.util.gradle.HierarchicalElementUtils
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils

@IgnoreIf({ JavaVersion.current().isJava9Compatible() }) // TODO update cglib and re-enable the test
class GradleClasspathContainerUpdaterTest extends WorkspaceSpecification {

    IJavaProject project

    def setup() {
        File folder = dir('another')
        File file = file('another/file')
        project = newJavaProject("sample")
        project.project.getFolder('linked_folder').createLink(new Path(folder.absolutePath), IResource.BACKGROUND_REFRESH | IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, new NullProgressMonitor())
        project.project.getFile('linked_file').createLink(new Path(file.absolutePath), IResource.BACKGROUND_REFRESH | IResource.ALLOW_MISSING_LOCAL | IResource.REPLACE, new NullProgressMonitor())
        project.setRawClasspath([JavaCore.newContainerEntry(GradleClasspathContainer.CONTAINER_PATH)] as IClasspathEntry[], null)
    }

    def "Nonexisting resources are also added to the classpath"() {
        given:
        def file = new File("nonexisting.jar")

        def gradleProject = gradleProjectWithClasspath(
            externalDependency(file)
        )
        PersistentModelBuilder persistentModel = persistentModelBuilder(project.project)

        when:
        Set allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toFile() == file.absoluteFile
    }

    def "Folders are valid external dependencies"() {
        given:
        def gradleProject = gradleProjectWithClasspath(
            externalDependency(dir("foo"))
        )
        PersistentModelBuilder persistentModel = persistentModelBuilder(project.project)

        when:
        Set allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toFile().canonicalPath.equals(dir("foo").canonicalPath)
    }

    def "Linked files can be added to the classpath"(String path) {
        given:
        def gradleProject = gradleProjectWithClasspath(
            externalDependency(new File(path))
        )
        PersistentModelBuilder persistentModel = persistentModelBuilder(project.project)

        when:
        Set allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toPortableString() == '/sample/linked_file'

        where:
        path << ['linked_file', '/linked_file']
    }

    def "Linked folders can be added to the classpath"(String path) {
        given:
        def gradleProject = gradleProjectWithClasspath(
            externalDependency(new File(path))
        )
        PersistentModelBuilder persistentModel = persistentModelBuilder(project.project)

        when:
        Set allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toPortableString() == '/sample/linked_folder'

        where:
        path << ['linked_folder', '/linked_folder']
    }

    def "Verify container is only updated on project, if content has changed"(){
        given:
        def gradleProject = gradleProjectWithClasspath(
                externalDependency(dir("foo")),
                externalDependency(dir("bar"))
                )
        PersistentModelBuilder persistentModel = persistentModelBuilder(project.project)

        expect:
        def initialContainer = gradleClasspathContainer
        initialContainer

        when:
        Set allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        def modifiedContainer = gradleClasspathContainer
        !modifiedContainer.is(initialContainer)

        when:
        persistentModel = persistentModelBuilder(persistentModel.build())
        allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        modifiedContainer.is(gradleClasspathContainer)
    }

    def "Non-lowercase extensions should be taken into account"(String path) {
        given:
        def file = new File(path)

        def gradleProject = gradleProjectWithClasspath(
            externalDependency(file)
        )
        PersistentModelBuilder persistentModel = persistentModelBuilder(project.project)

        when:
        Set allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        resolvedClasspath[0].entryKind == IClasspathEntry.CPE_LIBRARY
        resolvedClasspath[0].path.toFile() == file.absoluteFile

        where:
        path << ['test.Zip', 'test.ZIP', 'test.rar', 'test.RAR']
    }

    def "Non-zip files should be ignored"() {
        given:
        def file = new File("test.dll")

        def gradleProject = gradleProjectWithClasspath(
            externalDependency(file)
        )
        PersistentModelBuilder persistentModel = persistentModelBuilder(project.project)

        when:
        Set allProjects = HierarchicalElementUtils.getAll(gradleProject).toSet()
        GradleClasspathContainerUpdater.updateFromModel(project, gradleProject, allProjects, persistentModel, null, null)

        then:
        resolvedClasspath.length == 0
    }

    EclipseProject gradleProjectWithClasspath(Object... dependencies) {
        Stub(EclipseProject) {
            getClasspath() >> ModelUtils.asDomainObjectSet(dependencies.findAll { it instanceof EclipseExternalDependency })
            getProjectDependencies() >> ModelUtils.asDomainObjectSet(dependencies.findAll { it instanceof EclipseProjectDependency })
        }
    }

    EclipseExternalDependency externalDependency(File location, File sources = null,
                                              File javaDoc = null, boolean exported = false,
                                              List attributes = [], List rules = []) {
        Stub(EclipseExternalDependency) {
            getFile() >> location
            getSource() >> sources
            getJavadoc() >> javaDoc
            isExported() >> exported
            getClasspathAttributes() >> ModelUtils.asDomainObjectSet(attributes)
            getAccessRules() >> ModelUtils.asDomainObjectSet(rules)
        }
    }

    IClasspathEntry[] getResolvedClasspath() {
        project.getResolvedClasspath(false)
    }

    GradleClasspathContainer getGradleClasspathContainer() {
        JavaCore.getClasspathContainer(GradleClasspathContainer.CONTAINER_PATH, project)
    }
}
