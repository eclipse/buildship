package org.eclipse.buildship.core.internal.workspace.impl

import org.gradle.api.JavaVersion
import spock.lang.Unroll

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.Path
import org.eclipse.jdt.core.IAccessRule
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.internal.Logger
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature
import org.eclipse.buildship.core.internal.configuration.impl.BuildConfigurationPersistence
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution
import org.eclipse.buildship.core.internal.util.gradle.JavaVersionUtil
import org.eclipse.buildship.core.internal.workspace.GradleClasspathContainer

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

        then:
        def project = findProject('sample-project')
        project.hasNature(GradleProjectNature.ID)
    }

    @Unroll
    def "Natures and build commands are updated for Gradle #distribution.configuration"(GradleDistribution distribution) {
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
        importAndWait(projectDir, distribution)

        then:
        def project = findProject('sample-project')
        project.description.natureIds.find { it == 'org.eclipse.pde.UpdateSiteNature' }
        project.description.buildSpec.find { it.builderName == 'customBuildCommand' }.arguments == ['buildCommandKey' : "buildCommandValue"]

        where:
        distribution << supportedGradleDistributions
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
        new BuildConfigurationPersistence().readPathToRoot(project) == ''
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

    @Unroll
    def "Source settings are updated for #distribution.configuration"(GradleDistribution distribution) {
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
        importAndWait(projectDir, distribution)
        IJavaProject javaProject = findJavaProject('sample-project')
        String sourceCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)
        String sourceCompatibility = javaProject.getOption(JavaCore.COMPILER_SOURCE, true)
        String targetCompatibility = javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true)
        String currentJavaVersion = JavaVersionUtil.adaptVersionToEclipseNamingConversions(JavaVersion.current())

        then:
        sourceCompliance == JavaCore.VERSION_1_2
        sourceCompatibility == JavaCore.VERSION_1_2
        targetCompatibility == JavaCore.VERSION_1_3

        where:
        distribution << getSupportedGradleDistributions('>=2.11')
    }

    def "Target compatibility reuses source compatibility for Gradle 2.10"() {
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
        importAndWait(projectDir, GradleDistribution.forVersion('2.10'))
        IJavaProject javaProject = findJavaProject('sample-project')
        String sourceCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)
        String sourceCompatibility = javaProject.getOption(JavaCore.COMPILER_SOURCE, true)
        String targetCompatibility = javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true)
        String currentJavaVersion = JavaVersionUtil.adaptVersionToEclipseNamingConversions(JavaVersion.current())

        then:
        sourceCompliance == JavaCore.VERSION_1_2
        sourceCompatibility == JavaCore.VERSION_1_2
        targetCompatibility == JavaCore.VERSION_1_2
    }

    @Unroll
    def "Source settings match current Java version for #distribution.configuration"(GradleDistribution distribution) {
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
        importAndWait(projectDir, distribution)
        IJavaProject javaProject = findJavaProject('sample-project')
        String sourceCompliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true)
        String sourceCompatibility = javaProject.getOption(JavaCore.COMPILER_SOURCE, true)
        String targetCompatibility = javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true)
        String currentJavaVersion = JavaVersionUtil.adaptVersionToEclipseNamingConversions(JavaVersion.current())

        then:
        assert sourceCompliance == currentJavaVersion
        assert sourceCompatibility == currentJavaVersion
        assert targetCompatibility == currentJavaVersion

        where:
        distribution << getSupportedGradleDistributions('<2.10')
    }

    @Unroll
    def "Source folders are not updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareJavaProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', '''
                apply plugin: 'java'

                sourceSets {
                    main {
                        java {
                            exclude 'excludePattern'
                            include 'includePattern'
                        }
                    }
                }
            '''
            dir 'src/main/java'
        }

        when:
        importAndWait(projectDir, distribution)
        IJavaProject javaProject = findJavaProject('sample-project')
        IClasspathEntry sourceDir = javaProject.rawClasspath.find { it.entryKind == IClasspathEntry.CPE_SOURCE && it.path.toPortableString() == '/sample-project/src/main/java' }

        then:
        sourceDir.exclusionPatterns.length == 0
        sourceDir.inclusionPatterns.length == 0

        where:
        distribution << getSupportedGradleDistributions('<3.0')
    }


    @Unroll
    def "Source folders are updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareJavaProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', '''
                apply plugin: 'java'

                sourceSets {
                    main {
                        java {
                            exclude 'excludePattern'
                            include 'includePattern'
                        }
                    }
                }
            '''
            dir 'src/main/java'
        }

        when:
        importAndWait(projectDir, distribution)
        IJavaProject javaProject = findJavaProject('sample-project')
        IClasspathEntry sourceDir = javaProject.rawClasspath.find { it.entryKind == IClasspathEntry.CPE_SOURCE && it.path.toPortableString() == '/sample-project/src/main/java' }

        then:
        sourceDir.exclusionPatterns.collect { it.toPortableString() } == ['excludePattern']
        sourceDir.inclusionPatterns.collect { it.toPortableString() } == ['includePattern']

        where:
        distribution << getSupportedGradleDistributions('>=3.0')
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

    @Unroll
    def "Custom classpath containers are not updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                eclipse {
                    classpath {
                        containers 'custom.container', 'second.container'
                    }
                }
            """
            dir 'src/main/java'
        }

        when:
        importAndWait(projectDir, distribution)
        List containers = findJavaProject('sample-project').rawClasspath.findAll { it.entryKind == IClasspathEntry.CPE_CONTAINER }.collect { it.path.segment(0) }

        then:
        containers == [JavaRuntime.JRE_CONTAINER] + GradleClasspathContainer.CONTAINER_PATH.toPortableString()

        where:
        distribution << getSupportedGradleDistributions('<3.0')
    }

    @Unroll
    def "Custom classpath containers are updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            file 'build.gradle', """
                apply plugin: 'java'
                apply plugin: 'eclipse'
                eclipse {
                    classpath {
                        containers 'custom.container', 'second.container'
                    }
                }
            """
            dir 'src/main/java'
        }

        when:
        importAndWait(projectDir, distribution)
        List containers = findJavaProject('sample-project').rawClasspath.findAll { it.entryKind == IClasspathEntry.CPE_CONTAINER }.collect { it.path.segment(0) }

        then:
        containers == [JavaRuntime.JRE_CONTAINER] + 'custom.container' + 'second.container' + GradleClasspathContainer.CONTAINER_PATH.toPortableString()

        where:
        distribution << getSupportedGradleDistributions('>=3.0')
    }

    @Unroll
    def "Custom project output location is not updated for #distribution.configuration"(GradleDistribution distribution) {
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
        importAndWait(projectDir, distribution)
        String outputLocation = findJavaProject('sample-project').outputLocation?.toPortableString()

        then:
        outputLocation == '/sample-project/bin'

        where:
        distribution << getSupportedGradleDistributions('<3.0')
    }

    @Unroll
    def "Custom project output location is updated for #distribution.configuration"(GradleDistribution distribution) {
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
        importAndWait(projectDir, distribution)
        String outputLocation = findJavaProject('sample-project').outputLocation?.toPortableString()

        then:
        outputLocation == '/sample-project/target/bin'

        where:
        distribution <<  getSupportedGradleDistributions('>=3.0')
    }

    @Unroll
    def "Custom access rules are not updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            dir('api')
            dir('src/main/java')

            file 'settings.gradle', 'include "api"'

            file 'build.gradle', """
                import org.gradle.plugins.ide.eclipse.model.AccessRule

                allprojects {
                    apply plugin: 'java'
                    apply plugin: 'eclipse'
                    ${jcenterRepositoryBlock}
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
           importAndWait(projectDir, distribution)
           IJavaProject project = findJavaProject('sample-project')
           IClasspathEntry projectDep = project.getResolvedClasspath(true).find { it.path.toPortableString() == '/api' }
           IClasspathEntry libraryDep = project.getResolvedClasspath(true).find { it.path.toPortableString().endsWith 'guava-18.0.jar' }

           then:
           assertNoAccessRules(projectDep)
           assertNoAccessRules(libraryDep)

           where:
           distribution << getSupportedGradleDistributions('<3.0')
    }

    @Unroll
    def "Custom access rules are updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            dir('api')
            dir('src/main/java')

            file 'settings.gradle', 'include "api"'

            file 'build.gradle', """
                import org.gradle.plugins.ide.eclipse.model.AccessRule

                allprojects {
                    apply plugin: 'java'
                    apply plugin: 'eclipse'
                    ${jcenterRepositoryBlock}
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
           importAndWait(projectDir, distribution)
           IJavaProject project = findJavaProject('sample-project')
           IClasspathEntry container = project.rawClasspath.find { it.path.toPortableString() == 'containerPath' }
           IClasspathEntry projectDep = project.getResolvedClasspath(true).find { it.path.toPortableString() == '/api' }
           IClasspathEntry libraryDep = project.getResolvedClasspath(true).find { it.path.toPortableString().endsWith 'guava-18.0.jar' }

           then:
           assertAccessRules(container, IAccessRule.K_ACCESSIBLE, 'container-pattern')
           assertAccessRules(projectDep, IAccessRule.K_NON_ACCESSIBLE, 'project-pattern')
           assertAccessRules(libraryDep, IAccessRule.K_DISCOURAGED, 'library-pattern')

           where:
           distribution <<  getSupportedGradleDistributions('>=3.0')
    }

    @Unroll
    def "Custom source folder output location is updated for #distribution.configuration"(GradleDistribution distribution) {
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
        importAndWait(projectDir, distribution)
        IClasspathEntry sourceDir = findJavaProject('sample-project').rawClasspath.find { it.path.toPortableString() == '/sample-project/src/main/java' }
        String outputLocation = sourceDir.outputLocation?.toPortableString()

        then:
        outputLocation == '/sample-project/target/classes'

        where:
        distribution << getSupportedGradleDistributions('>=3.0')
    }

    @Unroll
    def "Custom source folder output location is not updated for #distribution.configuration"(GradleDistribution distribution) {
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
        importAndWait(projectDir, distribution)
        IClasspathEntry sourceDir = findJavaProject('sample-project').rawClasspath.find { it.path.toPortableString() == '/sample-project/src/main/java' }
        String outputLocation = sourceDir.outputLocation?.toPortableString()

        then:
        !outputLocation

        where:
        distribution << getSupportedGradleDistributions('<3.0')
    }

    @Unroll
    def "Classpath attributes are updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            dir 'src/main/java'
            file 'settings.gradle', 'include "api"'
            file 'build.gradle', """
                allprojects {
                    apply plugin: 'java'
                    apply plugin: 'eclipse'
                    ${jcenterRepositoryBlock}
                }

                dependencies {
                    compile 'com.google.guava:guava:18.0'
                    compile project(':api')
                }

                eclipse {
                    classpath {
                        downloadSources = false
                        downloadJavadoc = true
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
           importAndWait(projectDir, distribution)
           IJavaProject project = findJavaProject('sample-project')
           IClasspathEntry source = project.rawClasspath.find { it.path.toPortableString() == '/sample-project/src/main/java' }
           IClasspathEntry container = project.rawClasspath.find { it.path.toPortableString() == 'containerPath' }
           IClasspathEntry projectDep = project.getResolvedClasspath(true).find { it.path.toPortableString() == '/api' }
           IClasspathEntry libraryDep = project.getResolvedClasspath(true).find { it.path.toPortableString().endsWith 'guava-18.0.jar' }

           then:
           assertClasspathAttributes(source, 'sourceKey', 'sourceValue')
           assertClasspathAttributes(container, 'containerKey', 'containerValue')
           assertClasspathAttributes(projectDep, 'projectKey', 'projectValue')
           assertClasspathAttributes(libraryDep, 'javadoc_location')
           assertClasspathAttributes(libraryDep, 'libraryKey', 'libraryValue')

           where:
           distribution << getSupportedGradleDistributions('>=3.0')
    }

    @Unroll
    def "Classpath attributes are not updated for #distribution.configuration"(GradleDistribution distribution) {
        setup:
        prepareProject('sample-project')
        def projectDir = dir('sample-project') {
            dir 'src/main/java'
            file 'settings.gradle', 'include "api"'
            file 'build.gradle', """
                allprojects {
                    apply plugin: 'java'
                    apply plugin: 'eclipse'
                    ${jcenterRepositoryBlock}
                }

                dependencies {
                    compile 'com.google.guava:guava:18.0'
                    compile project(':api')
                }

                eclipse {
                    classpath {
                        downloadSources = false
                        downloadJavadoc = true

                        file {
                            whenMerged { classpath ->
                                def source = classpath.entries.find { it.path == 'src/main/java' }
                                def container = classpath.entries.find { it.path == 'containerPath' }
                                def project = classpath.entries.find { it.path == '/api' }
                                def library = classpath.entries.find { it.path.endsWith 'guava-18.0.jar' }
                                source.entryAttributes.sourceKey = 'sourceValue'
                                project.entryAttributes.projectKey = 'projectValue'
                                library.entryAttributes.libraryKey = 'libraryValue'
                            }
                        }
                    }
                }
            """
           }

           when:
           importAndWait(projectDir, distribution)
           IJavaProject project = findJavaProject('sample-project')
           IClasspathEntry source = project.rawClasspath.find { it.path.toPortableString() == '/sample-project/src/main/java' }
           IClasspathEntry projectDep = project.getResolvedClasspath(true).find { it.path.toPortableString() == '/api' }
           IClasspathEntry libraryDep = project.getResolvedClasspath(true).find { it.path.toPortableString().endsWith 'guava-18.0.jar' }

           then:
           assertNoClasspathAttributes(source)
           assertNoClasspathAttributes(projectDep)
           assertNoClasspathAttributes(libraryDep)

           where:
           distribution << getSupportedGradleDistributions('<3.0')
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

    protected void assertNoAccessRules(IClasspathEntry entry) {
        assert entry.accessRules.length == 0
    }

    protected void assertClasspathAttributes(IClasspathEntry entry, String name) {
        assert entry.extraAttributes.find { it.name == name && it.value != null }
    }

    protected void assertClasspathAttributes(IClasspathEntry entry, String name, String value) {
        assert entry.extraAttributes.find { it.name == name && it.value == value }
    }

    protected void assertNoClasspathAttributes(IClasspathEntry entry) {
        assert entry.extraAttributes.length == 0
    }

    protected void assertNoClasspathAttributes(IClasspathEntry entry, String name) {
       assert !entry.extraAttributes.find { it.name == name }
    }
}
