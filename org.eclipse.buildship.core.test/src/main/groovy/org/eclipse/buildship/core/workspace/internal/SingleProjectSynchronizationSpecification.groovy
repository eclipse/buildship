package org.eclipse.buildship.core.workspace.internal

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.core.configuration.internal.DefaultProjectConfigurationPersistence
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.workspace.GradleClasspathContainer

abstract class SingleProjectSynchronizationSpecification extends ProjectSynchronizationSpecification {

    protected abstract void prepareProject(String name)

    protected abstract void prepareJavaProject(String name)

    def "The Gradle nature is set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        when:
        synchronizeAndWait(projectDir)

        expect:

        then:
        def project = findProject('sample-project')
        project.hasNature(GradleProjectNature.ID)
    }

    def "Natures and build commands are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        natures << "org.eclipse.pde.UpdateSiteNature"
                        buildCommand 'customBuildCommand', buildCommandKey: "buildCommandValue"
                    }
                }
            """
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        project.description.natureIds.find{ it == 'org.eclipse.pde.UpdateSiteNature' }
        project.description.buildSpec.find{ it.builderName == 'customBuildCommand' }.arguments == ['buildCommandKey' : "buildCommandValue"]
    }

    def "The Gradle settings file is written"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        new DefaultProjectConfigurationPersistence().readProjectConfiguration(project)
    }

    def "Derived resources are marked"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle'
            dir 'build'
            dir '.gradle'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        project.getFolder("build").isDerived()
        project.getFolder(".gradle").isDerived()
    }

    def "Invalid build folders are ignored"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                buildDir = "../build"
            """
        }
        Logger logger = Mock(Logger)
        environment.registerService(Logger, logger)

        when:
        synchronizeAndWait(projectDir)

        then:
        0 * logger.error(*_)
    }

    def "The build folder can be a linked resource"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle',
            '''
                apply plugin: "java"
                apply plugin: 'eclipse'
                buildDir = "../another-project/build"
                eclipse.project.linkedResource name:'build', type:'2', location: file('../another-project/build').path
            '''
            dir '../another-project/build'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        def build = project.getFolder('build')
        build.isLinked()
        build.isDerived()
    }

    def "Linked resources are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle',
            '''
                apply plugin: "java"
                sourceSets { main { java { srcDir '../another-project/src' } } }
            '''
            dir '../another-project/src'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject('sample-project')
        project.getFolder('src').isLinked()
    }

    def "Source settings are updated"() {
        setup:
        prepareJavaProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'java'
                sourceCompatibility = 1.2
                targetCompatibility = 1.3
            """
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def javaProject = findJavaProject('sample-project')
        javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true) == JavaCore.VERSION_1_2
        javaProject.getOption(JavaCore.COMPILER_SOURCE, true) == JavaCore.VERSION_1_2
        javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true) == JavaCore.VERSION_1_3
    }

    def "Source folders are updated"() {
        setup:
        prepareJavaProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', 'apply plugin: "java"'
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def javaProject = findJavaProject('sample-project')
        javaProject.rawClasspath.find{
            it.entryKind == IClasspathEntry.CPE_SOURCE &&
            it.path.toPortableString() == '/sample-project/src/main/java'
        }
    }

    def "If the project applies the java plugin, then it's converted to a Java project"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', 'apply plugin: "java"'
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        def project = findProject("sample-project")
        project.hasNature(JavaCore.NATURE_ID)
    }

    def "If the project applies the Java plugin then the Gradle classpath container is added after the JRE container"() {
        setup:
        prepareProject("sample-project")
        def projectDir = dir('sample-project') {
            file 'build.gradle', 'apply plugin: "java"'
            dir 'src/main/java'
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        List<IClasspathEntry> containers = findJavaProject('sample-project').rawClasspath.findAll {
            it.entryKind == IClasspathEntry.CPE_CONTAINER
        }

        containers.size() == 2
        containers[0].path.segment(0) == JavaRuntime.JRE_CONTAINER
        containers[1].path == GradleClasspathContainer.CONTAINER_PATH
    }

    def "Custom containers are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                eclipse {
                    classpath {
                        containers 'custom.container'
                    }
                }
            """
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        findJavaProject('sample-project').rawClasspath.find { IClasspathEntry entry -> entry.entryKind == IClasspathEntry.CPE_CONTAINER  && entry.path.toPortableString() == 'custom.container' }
    }

    def "Custom project output location is set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                eclipse {
                    classpath {
                        defaultOutputDir = file('target/bin')
                    }
                }
            """
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        findJavaProject('sample-project').getOutputLocation().toPortableString() == '/sample-project/target/bin'
    }

    def "Custom access rules are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'settings.gradle', 'include "api"'

            file 'build.gradle', """
                import org.gradle.plugins.ide.eclipse.model.AccessRule

                allprojects {
                    apply plugin: 'java'
                    apply plugin: 'eclipse'
                    repositories.jcenter()
                }

                dependencies {
                    compile 'com.google.guava:guava:18.0'
                    compile project(':api')
                }

                eclipse {
                    classpath {
                        containers 'containerPath'

                        file {
                            whenMerged { classpath ->
                                def container = classpath.entries.find { it.path == 'containerPath' }
                                def project = classpath.entries.find { it.path == '/api' }
                                def library = classpath.entries.find { it.path.endsWith 'guava-18.0.jar' }
                                container.accessRules.add(new AccessRule('0', 'container-pattern'))
                                project.accessRules.add(new AccessRule('1', 'project-pattern'))
                                library.accessRules.add(new AccessRule('2', 'library-pattern'))
                            }
                        }
                    }
                }
            """
           }

           when:
           synchronizeAndWait(projectDir)

           then:
           IJavaProject project = findJavaProject('sample-project')
           IClasspathEntry container = project.rawClasspath.find { it.path.toPortableString() == 'containerPath' }
           IClasspathEntry projectDep = project.getResolvedClasspath(true).find { it.path.toPortableString() == '/api' }
           IClasspathEntry libraryDep = project.getResolvedClasspath(true).find { it.path.toPortableString().endsWith 'guava-18.0.jar' }
           assertAccessRules(container, IAccessRule.K_ACCESSIBLE, 'container-pattern')
           assertAccessRules(projectDep, IAccessRule.K_NON_ACCESSIBLE, 'project-pattern')
           assertAccessRules(libraryDep, IAccessRule.K_DISCOURAGED, 'library-pattern')
    }

    def "Custom output folder is set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            dir 'src/main/java'
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                eclipse {
                    classpath {
                        file {
                            whenMerged { classpath ->
                                def src = classpath.entries.find { it.path == 'src/main/java' }
                                src.output = 'target/classes'
                            }
                        }
                    }
                }
            """
        }

        when:
        synchronizeAndWait(projectDir)

        then:
        IJavaProject project = findJavaProject('sample-project')
        project.rawClasspath.find { it.path.toPortableString() == '/sample-project/src/main/java' }.outputLocation.toPortableString() == '/sample-project/target/classes'
    }

    def "Custom classpath attributes are set"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            dir 'src/main/java'
            file 'settings.gradle', 'include "api"'
            file 'build.gradle', """
                allprojects {
                    apply plugin: 'java'
                    apply plugin: 'eclipse'
                    repositories.jcenter()
                }

                dependencies {
                    compile 'com.google.guava:guava:18.0'
                    compile project(':api')
                }

                eclipse {
                    classpath {
                        containers 'containerPath'

                        file {
                            whenMerged { classpath ->
                                def source = classpath.entries.find { it.path == 'src/main/java' }
                                def container = classpath.entries.find { it.path == 'containerPath' }
                                def project = classpath.entries.find { it.path == '/api' }
                                def library = classpath.entries.find { it.path.endsWith 'guava-18.0.jar' }
                                source.entryAttributes.sourceKey = 'sourceValue'
                                container.entryAttributes.containerKey = 'containerValue'
                                project.entryAttributes.projectKey = 'projectValue'
                                library.entryAttributes.libraryKey = 'libraryValue'
                            }
                        }
                    }
                }
            """
           }

           when:
           synchronizeAndWait(projectDir)

           then:
           IJavaProject project = findJavaProject('sample-project')
           IClasspathEntry source = project.rawClasspath.find { it.path.toPortableString() == '/sample-project/src/main/java' }
           IClasspathEntry container = project.rawClasspath.find { it.path.toPortableString() == 'containerPath' }
           IClasspathEntry projectDep = project.getResolvedClasspath(true).find { it.path.toPortableString() == '/api' }
           IClasspathEntry libraryDep = project.getResolvedClasspath(true).find { it.path.toPortableString().endsWith 'guava-18.0.jar' }
           assertClasspathAttributes(source, 'sourceKey', 'sourceValue')
           assertClasspathAttributes(container, 'containerKey', 'containerValue')
           assertClasspathAttributes(projectDep, 'projectKey', 'projectValue')
           assertClasspathAttributes(libraryDep, 'libraryKey', 'libraryValue')
    }

    def "Custom java runtime name"() {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                eclipse {
                    jdt {
                        javaRuntimeName = 'Domino'
                    }

                    classpath {
                        file {
                            whenMerged { classpath ->
                                def jre = classpath.entries.find { it.path.contains 'Domino' }
                                jre.path = jre.path.replace('StandardVMType', 'CustomVMType')
                            }
                        }
                    }
                }
            """
           }

           when:
           synchronizeAndWait(projectDir)

           then:
           IJavaProject project = findJavaProject('sample-project')
           IPath jrePath = new Path('org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.CustomVMType/Domino')
           project.rawClasspath.find { it.path == jrePath }
    }

    protected void assertAccessRules(IClasspathEntry entry, int kind, String pattern) {
        assert entry.accessRules.length == 1
        assert entry.accessRules[0].kind == kind
        assert entry.accessRules[0].pattern.toPortableString() == pattern
    }

    protected void assertClasspathAttributes(IClasspathEntry entry, String name, String value) {
        assert entry.extraAttributes.find { it.name == name && it.value == value }
    }
}
