package org.eclipse.buildship.ui.internal.launch

import org.eclipse.jdt.core.IJavaProject
import org.eclipse.jdt.core.IType

import org.eclipse.buildship.core.internal.util.gradle.GradleDistribution
import org.eclipse.buildship.core.internal.util.gradle.GradleVersion
import org.eclipse.buildship.ui.internal.launch.TestLaunchShortcutValidator.PropertyTester
import org.eclipse.buildship.ui.internal.test.fixtures.ProjectSynchronizationSpecification

class TestLaunchShortcutValidatorTest extends ProjectSynchronizationSpecification {

    def "Launch shortcut enabled on test sources"(String gradleVersion) {
        setup:
        importAndWait(projectWithSources, GradleDistribution.forVersion(gradleVersion))

        expect:
        numOfGradleErrorMarkers == 0

        when:
        IJavaProject project = findJavaProject('project-with-sources')
        IType type = project.findType('LibrarySpec')

        then:
        testLaunchShortcutEnabledOn(type)

        where:
        gradleVersion << ['4.3', GradleVersion.current().version]
    }

    def "Launch shortcut disabled on production sources"(String gradleVersion) {
        setup:
        importAndWait(projectWithSources, GradleDistribution.forVersion(gradleVersion))

        expect:
        numOfGradleErrorMarkers == 0

        when:
        IJavaProject project = findJavaProject('project-with-sources')
        IType type = project.findType('Library')

        then:
        !testLaunchShortcutEnabledOn(type)

        where:
        gradleVersion << ['4.3', GradleVersion.current().version]
    }

    def "Launch shortcut disabled on non-source type"() {
        setup:
        importAndWait(projectWithSources)

        expect:
        numOfGradleErrorMarkers == 0

        when:
        IJavaProject project = findJavaProject('project-with-sources')
        IType type = project.findType('com.google.common.base.Predicate')

        then:
        !testLaunchShortcutEnabledOn(type)
    }

    private File getProjectWithSources() {
        dir('project-with-sources') {
            file 'build.gradle', """
                apply plugin: 'java'

                ${jcenterRepositoryBlock}

                dependencies {
                    compile 'com.google.guava:guava:18.0'
                }
            """
            dir('src/main/java') {
                file  'Library.java', 'public class Library { }'
            }
            dir('src/test/java') {
                file  'LibrarySpec.java', 'public class LibrarySpec { }'
            }
        }
    }

    private boolean testLaunchShortcutEnabledOn(Object... receiver) {
        new PropertyTester().test(Arrays.asList(receiver), PropertyTester.PROPERTY_NAME_SELECTION_CAN_BE_LAUNCHED_AS_TEST, new Object[0], null)
    }

}
