package org.eclipse.buildship.core.internal.workspace.internal

import spock.lang.Issue

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.buildship.core.Logger
import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification

@Issue("https://bugs.eclipse.org/bugs/show_bug.cgi?id=497753")
class ImportingProjectWithCustomNature extends ProjectSynchronizationSpecification {

    // registered in the org.eclipse.buildship.core.test/fragment.xml file
    public static class JavaExtensionNature implements IProjectNature {
        void configure() { }
        void deconfigure() { }
        IProject getProject() { }
        void setProject(IProject project) { }
    }

    def "Can import project with custom project nature that depends on another nature"() {
        setup:
        Logger logger = Mock(Logger)
        environment.registerService(Logger, logger)
        File location = dir('project-with-custom-nature') {
            file 'build.gradle', '''
                apply plugin: 'eclipse'
                eclipse {
                    project {
                        natures 'org.eclipse.buildship.core.javaextensionnature', 'org.eclipse.jdt.core.javanature'
                    }
                }
            '''
        }

        when:
        importAndWait(location)

        then:
        0 * logger.error(*_)
    }
}
