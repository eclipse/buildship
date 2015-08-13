package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.jdt.core.IClasspathEntry
import org.eclipse.jdt.core.JavaCore

import org.eclipse.buildship.core.test.fixtures.ProjectImportSpecification


class DependencyExportTest extends ProjectImportSpecification {

    def "Transitive dependencies are acessible from local project classpath when using Gradle 2.5+"(GradleDistribution distribution) {
        setup:
        File location = multiProjectWithSpringTransitiveDependency()

        when:
        executeProjectImportAndWait(location, distribution)
        waitForJobsToFinish() // wait the classpath container to be resolved

        then:
        def moduleA = findProject('moduleA')
        def moduleB = findProject('moduleB')
        resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-beans-1.2.8.jar' }
        resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'moduleB' && entry.entryKind == IClasspathEntry.CPE_PROJECT && !entry.isExported() }
        resolvedClasspath(moduleB).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-beans-1.2.8.jar' && !entry.isExported() }

        where:
        distribution << [ GradleDistribution.forVersion('2.5'), GradleDistribution.forVersion('2.6') ]
    }

    def "Transitive dependencies are acessible via exports from dependent projects when using Gradle <2.5"(GradleDistribution distribution) {
        setup:
        File location = multiProjectWithSpringTransitiveDependency()

        when:
        executeProjectImportAndWait(location, distribution)
        waitForJobsToFinish() // wait the classpath container to be resolved

        then:
        def moduleA = findProject('moduleA')
        def moduleB = findProject('moduleB')
        !resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-beans-1.2.8.jar' }
        resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'moduleB' && entry.entryKind == IClasspathEntry.CPE_PROJECT && entry.isExported() }
        resolvedClasspath(moduleB).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-beans-1.2.8.jar' && entry.isExported() }

        where:
        distribution << [ GradleDistribution.forVersion('2.3'), GradleDistribution.forVersion('2.4') ]
    }

    def "Excluded dependencies (incorrectly) resolved from dependent projects when using Gradle <2.5"(GradleDistribution distribution) {
        setup:
        File location = springExampleProjectFromBug473348()

        when:
        executeProjectImportAndWait(location, distribution)
        waitForJobsToFinish() // wait the classpath container to be resolved

        then:
        def moduleA = findProject('moduleA')
        def moduleB = findProject('moduleB')

        resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-core-3.1.4.RELEASE.jar' }
        resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'moduleB' && entry.entryKind == IClasspathEntry.CPE_PROJECT && entry.isExported() }
        resolvedClasspath(moduleB).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-core-1.2.8.jar' && entry.isExported() }

        where:
        distribution << [ GradleDistribution.forVersion('2.3'), GradleDistribution.forVersion('2.4') ]
    }

    def "Excluded dependencies are not resolved when using Gradle 2.5+"(GradleDistribution distribution) {
        setup:
        File location = springExampleProjectFromBug473348()

        when:
        executeProjectImportAndWait(location, distribution)
        waitForJobsToFinish() // wait the classpath container to be resolved

        then:
        def moduleA = findProject('moduleA')
        def moduleB = findProject('moduleB')

        resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-core-3.1.4.RELEASE.jar' }
        !resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-core-1.2.8.jar' }
        resolvedClasspath(moduleA).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'moduleB' && entry.entryKind == IClasspathEntry.CPE_PROJECT && !entry.isExported() }
        resolvedClasspath(moduleB).any{ IClasspathEntry entry -> entry.path.lastSegment() == 'spring-core-1.2.8.jar' && !entry.isExported() }

        where:
        distribution << [ GradleDistribution.forVersion('2.5'), GradleDistribution.forVersion('2.6') ]
    }

    @Ignore // this test fails randomly and should be improved
    def "Sample should not compile when imported with Gradle version 2.4"() {
        setup:
        File location = springExampleProjectFromBug473348()

        when:
        executeProjectImportAndWait(location, GradleDistribution.forVersion('2.4'))
        waitForJobsToFinish() // wait for the compilation to be finished

        then:
        findProject('moduleA').findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length > 0;
        findProject('moduleB').findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length > 0;
    }

    @Ignore // this test fails randomly and should be improved
    def "Sample should compile when imported with Gradle version 2.5"() {
        setup:
        File location = springExampleProjectFromBug473348()

        when:
        executeProjectImportAndWait(location, GradleDistribution.forVersion('2.5'))
        waitForJobsToFinish() // wait for the compilation to be finished

        then:
        findProject('moduleA').findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length == 0;
        findProject('moduleB').findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length == 0;
    }

    private def multiProjectWithSpringTransitiveDependency() {
        // root
        file('springexample', 'build.gradle') <<
        '''allprojects {
               repositories { mavenCentral() }
               apply plugin: 'java'
           }
        '''
        file('springexample', 'settings.gradle') <<
        '''include "moduleA"
           include "moduleB"
        '''
        // moduleA
        folder('springexample', 'moduleA', 'src', 'main', 'java')
        file('springexample', 'moduleA', 'build.gradle') <<
        '''dependencies {
                compile (project(":moduleB"))
           }
        '''
        // moduleB
        folder('springexample', 'moduleB', 'src', 'main', 'java')
        file('springexample', 'moduleB', 'build.gradle') <<
        '''dependencies {
                compile "org.springframework:spring-beans:1.2.8"
           }
        '''

        folder('springexample')
    }

    private def springExampleProjectFromBug473348() {
        // root
        file('Bug473348', 'build.gradle') <<
        '''allprojects {
               repositories { mavenCentral() }
               apply plugin: 'java'
           }
        '''
        file('Bug473348', 'settings.gradle') <<
        '''include "moduleA"
           include "moduleB"
        '''
        // moduleA
        file('Bug473348', 'moduleA', 'build.gradle') <<
        '''dependencies {
                compile "org.springframework:spring-beans:3.1.4.RELEASE"
                compile (project(":moduleB")) {
                    exclude group: "org.springframework"
                }
           }
        '''
        file('Bug473348', 'moduleA', 'src', 'main', 'java', 'ApplicationA.java') <<
        '''import org.springframework.beans.BeansException;
           import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
           import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
           import java.beans.PropertyEditor;

           public class ApplicationA implements BeanFactoryPostProcessor {
               @Override
               public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                   try {
                       Class classA = Class.forName("any");
                       beanFactory.registerCustomEditor(classA, classA.asSubclass(PropertyEditor.class));
                   } catch (ClassNotFoundException e) {
                       e.printStackTrace();
                   }
               }
           }

        '''
        // moduleB
        file('Bug473348', 'moduleB', 'build.gradle') <<
        '''dependencies {
                compile "org.springframework:spring-beans:1.2.8"
           }
        '''
        file('Bug473348', 'moduleB', 'src', 'main', 'java', 'ApplicationB.java') <<
        '''import org.springframework.beans.factory.FactoryBean;
           public class ApplicationB
           {
               public void methodA(){
                   FactoryBean factoryBean;
               }
           }
        '''
        folder('Bug473348')
    }

    private def resolvedClasspath(IProject project) {
        JavaCore.create(project).getResolvedClasspath(false)
    }

    private def rawClasspath(IProject project) {
        JavaCore.create(project).rawClasspath
    }
}
