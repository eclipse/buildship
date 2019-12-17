package org.eclipse.buildship.core.internal.workspace

import org.gradle.api.JavaVersion
import org.gradle.tooling.model.eclipse.EclipseClasspathContainer
import org.gradle.tooling.model.eclipse.EclipseJavaSourceSettings
import org.gradle.tooling.model.eclipse.EclipseProject
import org.gradle.tooling.model.java.InstalledJdk

import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.launching.IRuntimeClasspathEntry
import org.eclipse.jdt.launching.JavaRuntime

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.internal.launch.SupportedLaunchConfigType
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.core.internal.util.gradle.CompatEclipseProject
import org.eclipse.buildship.core.internal.util.gradle.ModelUtils

class ClasspathDeduplicationTest extends ProjectSynchronizationSpecification {


    File sampleDir
    File commonDir
    File apiDir
    File implDir

    def setup() {
        createSampleProject()
        importAndWait(sampleDir, GradleDistribution.forVersion("4.4.1"))
    }

    def "Exported classpath entries are deduplicated"() {
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
                           compile "org.springframework:spring-core:1.2.8"
                       }
                   """
                   dir 'src/main/java'
               }
               apiDir = api {
                   file 'build.gradle', """
                       dependencies {
                           compile (project(":common"))
                           compile "org.springframework:spring-beans:1.2.8"
                       }
                   """
                   dir 'src/main/java'
               }
               implDir = impl {
                   file 'build.gradle', """
                       dependencies {
                           compile (project(":common"))
                           compile (project(":api"))
                       }
                   """
                   dir 'src/main/java'
               }
           }
       }
    }
}
