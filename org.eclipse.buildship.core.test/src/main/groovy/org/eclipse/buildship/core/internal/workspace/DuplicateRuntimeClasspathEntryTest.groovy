package org.eclipse.buildship.core.internal.workspace

import static org.gradle.api.JavaVersion.VERSION_13

import org.gradle.api.JavaVersion
import spock.lang.IgnoreIf

import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.launching.IRuntimeClasspathEntry
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.launch.SupportedLaunchConfigType
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class DuplicateRuntimeClasspathEntryTest extends ProjectSynchronizationSpecification {


    File sampleDir
    File commonDir
    File apiDir
    File implDir

    def setup() {
        createSampleProject()
        importAndWait(sampleDir, GradleDistribution.forVersion("5.5.1"))
    }

    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_13) }) // Gradle 5.5 can run on Java 12 and below
    def "Duplicate runtime classpath entries are removed"() {
        setup:
        ILaunchConfiguration launchConfig = createLaunchConfig(SupportedLaunchConfigType.JDT_JAVA_APPLICATION.id)

        when:
        synchronizeAndWait(findProject('sample'))

        then:
        IJavaProject project = findJavaProject('impl')
        project != null

        IRuntimeClasspathEntry unresolvedClasspath = JavaRuntime.newDefaultProjectClasspathEntry(project)
        IRuntimeClasspathEntry[] resolvedClasspath = JavaRuntime.resolveRuntimeClasspathEntry(unresolvedClasspath, launchConfig)
        Set<IRuntimeClasspathEntry> deduplicatedClasspath = new HashSet<>(Arrays.asList(resolvedClasspath))

        resolvedClasspath.length == deduplicatedClasspath.size()
    }

    private File createSampleProject() {
        dir('root') {
           sampleDir = sample {
               file 'build.gradle', """
                    allprojects {
                        ${jcenterRepositoryBlock}
                        apply plugin: 'java'
                    }
                    configure(subprojects) { subproject ->
                        apply plugin: 'eclipse'
                        eclipse {
                             classpath {
                                 file{
                                     whenMerged { classpath ->
                                         def exportCandidates = classpath.entries.findAll { entry -> entry.kind == 'lib' || entry.kind == 'src' && entry.path.startsWith('/') }
                                         exportCandidates*.exported = true
                                     }
                                 }
                             }
                        }
                    }
                """
               file 'settings.gradle', """
                    include 'common'
                    include 'api'
                    include 'impl'
                """
               commonDir = common {
                   file 'build.gradle', """
                       dependencies {
                           implementation "org.springframework:spring-core:1.2.8"
                       }
                   """
                   dir 'src/main/java'
               }
               apiDir = api {
                   file 'build.gradle', """
                       dependencies {
                           implementation (project(":common"))
                           implementation "org.springframework:spring-beans:1.2.8"
                       }
                   """
                   dir 'src/main/java'
               }
               implDir = impl {
                   file 'build.gradle', """
                       dependencies {
                           implementation (project(":common"))
                           implementation (project(":api"))
                       }
                   """
                   dir 'src/main/java'
               }
           }
       }
    }
}
