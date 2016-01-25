package org.eclipse.buildship.core.workspace.internal

import org.gradle.api.JavaVersion
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.internal.ProjectConfigurationPersistence
import org.eclipse.buildship.core.test.fixtures.BuildshipTestSpecification
import org.eclipse.buildship.core.test.fixtures.EclipseProjects
import org.eclipse.buildship.core.test.fixtures.FileStructure
import org.eclipse.buildship.core.test.fixtures.GradleModel
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.workspace.ExistingDescriptorHandler
import org.eclipse.buildship.core.workspace.GradleClasspathContainer
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResourceFilterDescription
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.JavaModelException;

class DefaultWorkspaceGradleOperationsTest extends BuildshipTestSpecification {

    //
    // Section #1: If there is a project in the workspace at the location of the Gradle project.
    //

    def "If workspace project exists at model location and closed, then the project remins untouched"() {
        setup:
        IProject project = newClosedProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        File[] projectFiles = folder('sample-project').listFiles()
        Long[] modifiedTimes = folder('sample-project').listFiles().collect{ it.lastModified() }

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        !project.isOpen()
        projectFiles == folder('sample-project').listFiles()
        modifiedTimes == folder('sample-project').listFiles().collect{ it.lastModified() }
    }

    def "If workspace project exists at model location, then the Gradle nature is set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.hasNature(GradleProjectNature.ID)
    }

    def "If workspace project exists at model location, then the additional natures and build commands are set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle', """
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        natures << "org.eclipse.pde.UpdateSiteNature"
                        buildCommand 'customBuildCommand', buildCommandKey: "buildCommandValue"
                    }
                }
            """
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
        project.description.buildSpec.find{ it.builderName == 'customBuildCommand' }.arguments == ['buildCommandKey' : "buildCommandValue"]
    }

    def "If workspace project exists at model location, then the Gradle settings file is written"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        new ProjectConfigurationPersistence().readProjectConfiguration(project)
    }

    def "If workspace project exists at model location, then resource filters are set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        project.filters.length == 0

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.filters.length == 2
        project.filters[0].type == IResourceFilterDescription.EXCLUDE_ALL.or(IResourceFilterDescription.FOLDERS).or(IResourceFilterDescription.INHERITABLE)
        project.filters[1].type == IResourceFilterDescription.EXCLUDE_ALL.or(IResourceFilterDescription.FOLDERS).or(IResourceFilterDescription.INHERITABLE)
        project.filters[0].fileInfoMatcherDescription.id == 'org.eclipse.ui.ide.multiFilter'
        project.filters[1].fileInfoMatcherDescription.id == 'org.eclipse.ui.ide.multiFilter'
        (project.filters[0].fileInfoMatcherDescription.arguments as String).endsWith("build")
        (project.filters[1].fileInfoMatcherDescription.arguments as String).endsWith(".gradle")
    }

    def "If workspace project exists at model location, then the linked resources are set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle',
            '''apply plugin: "java"
               sourceSets { main { java { srcDir '../another-project/src' } } }
            '''
            file 'sample-project/settings.gradle'
            folder 'another-project/src'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').getFolder('src').isLinked()
    }

    def "If workspace project exists at model location, then the source settings are updated"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle', """
                apply plugin: 'java'
                sourceCompatibility = 1.2
                targetCompatibility = 1.3
            """
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)
        javaProject = JavaCore.create(findProject('sample-project'))

        then:
        javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true) == JavaVersion.current().toString()
        javaProject.getOption(JavaCore.COMPILER_SOURCE, true) == JavaCore.VERSION_1_2
        javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == JavaCore.VERSION_1_3
    }

    def "If workspace project exists at model location, then an existing java project's source folders are updated"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle', 'apply plugin: "java"'
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        javaProject.project.hasNature(JavaCore.NATURE_ID)

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        javaProject.rawClasspath.find{
            it.entryKind == IClasspathEntry.CPE_SOURCE &&
            it.path.toPortableString() == '/sample-project/src/main/java' &&
            it.extraAttributes.length == 1
            it.extraAttributes[0].name == "FROM_GRADLE_MODEL"
        }
    }

    def "If workspace project exists at model location, then update the Gradle classpath container"() {
        setup:
        IJavaProject javaProject = newJavaProject('sample-project')
        IClasspathEntry[] entries = javaProject.rawClasspath + GradleClasspathContainer.newClasspathEntry()
        javaProject.setRawClasspath(entries, null)
        fileStructure().create {
            file 'sample-project/build.gradle','''apply plugin: "java"
               repositories { jcenter() }
               dependencies { compile "org.springframework:spring-beans:1.2.8"}
            '''
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        !javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        javaProject.getResolvedClasspath(false).find{ it.path.toPortableString().endsWith('spring-beans-1.2.8.jar') }
    }

    def "If workspace project exists at model location and the project applies the java plugin, then it's converted to a Java project"() {
        setup:
        IProject project = newOpenProject('sample-project')
        fileStructure().create {
            file 'sample-project/build.gradle', 'apply plugin: "java"'
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project.hasNature(JavaCore.NATURE_ID)
        JavaCore.create(project).rawClasspath.find{
            it.entryKind == IClasspathEntry.CPE_CONTAINER &&
            it.path.toPortableString() == GradleClasspathContainer.CONTAINER_ID
        }

    }

    //
    // Section #2: If there is an Eclipse project at the location of the Gradle project, i.e. there is a .project file
    //             in that folder.
    //

    def "If .project file exists at the model location, then the project is added to the workspace"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()
        file('sample-project/.project').exists()

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    def "If .project file exists at the model location, then the Gradle nature is set"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').hasNature(GradleProjectNature.ID)
    }

    def "If .project file exists at the model location, then the Gradle settings file is written"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        new ProjectConfigurationPersistence().readProjectConfiguration(project)
    }

    //
    // Section #3: If the there is no project in the workspace nor an Eclipse project at the location of the Gradle
    //             build
    //

    def "If no workspace project or .project file exists, then the project is created and added to the workspace"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        expect:
        CorePlugin.workspaceOperations().getAllProjects().isEmpty()

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        CorePlugin.workspaceOperations().allProjects.size() == 1
        findProject('sample-project')
    }

    def "If no workspace project or .project file exists, then the Gradle nature is set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').hasNature(GradleProjectNature.ID)
    }


    def "If no workspace project or .project file exists, then the settings file is written"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        new ProjectConfigurationPersistence().readProjectConfiguration(findProject('sample-project'))
    }

    def "If no workspace project or .project file exists, then the resource filters are set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        IProject project = findProject('sample-project')
        project.filters.length == 2
        project.filters[0].type == IResourceFilterDescription.EXCLUDE_ALL.or(IResourceFilterDescription.FOLDERS).or(IResourceFilterDescription.INHERITABLE)
        project.filters[1].type == IResourceFilterDescription.EXCLUDE_ALL.or(IResourceFilterDescription.FOLDERS).or(IResourceFilterDescription.INHERITABLE)
        project.filters[0].fileInfoMatcherDescription.id == 'org.eclipse.ui.ide.multiFilter'
        project.filters[1].fileInfoMatcherDescription.id == 'org.eclipse.ui.ide.multiFilter'
        (project.filters[0].fileInfoMatcherDescription.arguments as String).endsWith("build")
        (project.filters[1].fileInfoMatcherDescription.arguments as String).endsWith(".gradle")
    }

    def "If no workspace project or .project file exists, then the linked resources are set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle',
            '''apply plugin: "java"
               sourceSets { main { java { srcDir '../another-project/src' } } }
            '''
            file 'sample-project/settings.gradle'
            folder 'another-project/src'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        findProject('sample-project').getFolder('src').isLinked()
    }

    def "If no workspace project or .project file exists, then a Java project is created with the Gradle classpath container"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle', 'apply plugin: "java"'
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        def project = findProject('sample-project')
        project.hasNature(JavaCore.NATURE_ID)
        JavaCore.create(project).rawClasspath.find{
            it.entryKind == IClasspathEntry.CPE_CONTAINER &&
            it.path.toPortableString() == GradleClasspathContainer.CONTAINER_ID
        }
    }

    def "If no workspace project or .project file exists, then a Java project is created with proper source settings"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle', """
                apply plugin: "java"
                sourceCompatibility = 1.3
                targetCompatibility = 1.4
            """
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        def javaProject = JavaCore.create(findProject('sample-project'))
        javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true) == JavaVersion.current().toString()
        javaProject.getOption(JavaCore.COMPILER_SOURCE, true) == JavaCore.VERSION_1_3
        javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == JavaCore.VERSION_1_4
    }

    def "If no workspace project or .project file exists, then the additional natures and build commands are set"() {
        setup:
        fileStructure().create {
            file 'sample-project/build.gradle', """
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        natures << "org.eclipse.pde.UpdateSiteNature"
                        buildCommand 'customBuildCommand', buildCommandKey: "buildCommandValue"
                    }
                }
            """
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        def project = findProject('sample-project')
        project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
        project.description.buildSpec.find{ it.builderName == 'customBuildCommand' }.arguments == ['buildCommandKey' : "buildCommandValue"]
    }


    //
    // Section #4: If there is an existing .project file
    //

    def "If the project descriptor is overwritten on import, then the settings are synchronized with the Gradle build"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle', """
                apply plugin: 'java'
            """
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel, ExistingDescriptorHandler.ALWAYS_OVERWRITE)

        then:
        project.hasNature(GradleProjectNature.ID)
        project.hasNature(JavaCore.NATURE_ID)
        def javaProject = JavaCore.create(project)
        javaProject.rawClasspath.find{
            it.entryKind == IClasspathEntry.CPE_CONTAINER &&
            it.path.toPortableString() == GradleClasspathContainer.CONTAINER_ID
        }
    }

    def "If the project descriptor is overwritten on import, then all existing settings are removed"() {
        setup:
        IProject project = newJavaProject('sample-project').project
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle', """
            """
            file 'sample-project/settings.gradle'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel, ExistingDescriptorHandler.ALWAYS_OVERWRITE)

        then:
        project.hasNature(GradleProjectNature.ID)
        !project.hasNature(JavaCore.NATURE_ID)

        when:
        JavaCore.create(project).rawClasspath

        then:
        thrown JavaModelException
    }

    def "If the project descriptor is kept on import, then no settings are overwritten"() {
        setup:
        IProject project = newOpenProject('sample-project')
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/build.gradle', """
                apply plugin: 'java'
            """
            file 'sample-project/settings.gradle'
            folder 'sample-project/src/main/java'
        }
        GradleModel gradleModel = loadGradleModel('sample-project')

        when:
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel, ExistingDescriptorHandler.ALWAYS_KEEP)

        then:
        project.hasNature(GradleProjectNature.ID)
        !project.hasNature(JavaCore.NATURE_ID)
    }

    def "All subprojects with existing .project files are handled by the ExistingDescriptorHandler"() {
        setup:
        EclipseProjects.newProject('subproject-a', folder('sample-project/subproject-a'))
        EclipseProjects.newProject('subproject-b', folder('sample-project/subproject-b'))
        CorePlugin.workspaceOperations().deleteAllProjects(new NullProgressMonitor())
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        ExistingDescriptorHandler handler = Mock(ExistingDescriptorHandler)

        when:
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel, handler)

        then:
        2 * handler.shouldOverwriteDescriptor(_)
    }

    def "Uncoupling a project removes the Gradle nature"() {
        setup:
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        findProject('subproject-a').hasNature(GradleProjectNature.ID)

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        !findProject('subproject-a').hasNature(GradleProjectNature.ID)
    }

    def "Uncoupling a project removes the resource filters"() {
        setup:
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        IProject project = findProject('subproject-a')
        project.filters.length == 2
        project.filters[0].fileInfoMatcherDescription.arguments.contains("build")
        project.filters[1].fileInfoMatcherDescription.arguments.contains("gradle")

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project == findProject('subproject-a')
        project.filters.length == 0
    }

    def "Uncoupling a project removes the settings file"() {
        setup:
        fileStructure().create {
            file 'sample-project/subproject-a/build.gradle'
            file 'sample-project/subproject-b/build.gradle'
            file 'sample-project/build.gradle'
            file 'sample-project/settings.gradle', "include 'subproject-a', 'subproject-b'"
        }
        GradleModel gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        expect:
        IProject project = findProject('subproject-a')
        new File(project.location.toFile(), '.settings/gradle.prefs').exists()

        when:
        fileStructure().create { file 'sample-project/settings.gradle', "'subproject-b'" }
        gradleModel = loadGradleModel('sample-project')
        executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(gradleModel)

        then:
        project == findProject('subproject-a')
        !new File(project.location.toFile(), '.settings/gradle.prefs').exists()
    }

    // -- helper methods --

    private static def executeSynchronizeGradleProjectWithWorkspaceProjectAndWait(GradleModel gradleModel, ExistingDescriptorHandler existingDescriptorHandler = ExistingDescriptorHandler.ALWAYS_KEEP) {
        // Note: executing the synchronizeGradleProjectWithWorkspaceProject() in a new job is necessary
        // as the jdt operations expect that all modifications are guarded by proper rules. For the sake
        // of this test class we simply use the workspace root as the job rule.
        Job job = new Job('') {
            protected IStatus run(IProgressMonitor monitor) {
                Job.jobManager.beginRule(LegacyEclipseSpockTestHelper.workspace.root, monitor)
                new DefaultWorkspaceGradleOperations().synchronizeGradleBuildWithWorkspace(gradleModel.build, gradleModel.attributes, [], existingDescriptorHandler, new NullProgressMonitor())
                Job.jobManager.endRule(LegacyEclipseSpockTestHelper.workspace.root)
                Status.OK_STATUS
            }
        }
        job.schedule()
        waitForJobsToFinish()
    }

    private IProject newClosedProject(String name) {
        EclipseProjects.newClosedProject(name, folder(name))
    }

    private IProject newOpenProject(String name) {
        EclipseProjects.newProject(name, folder(name))
    }

    private IJavaProject newJavaProject(String name) {
        EclipseProjects.newJavaProject(name, folder(name))
    }

    private FileStructure fileStructure() {
        new FileStructure(externalTestFolder){}
    }

    private GradleModel loadGradleModel(String location) {
        GradleModel.fromProject(folder(location))
    }

    private IProject findProject(String name) {
        CorePlugin.workspaceOperations().findProjectByName(name).orNull()
    }

}
