/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Ian Stewart-Binks (Red Hat) - Smart Import feature
 */

package org.eclipse.buildship.ui.smartimport

import static org.junit.Assert.*
import static org.eclipse.buildship.ui.smartimport.ImporterTestUtilities.*

import spock.lang.Specification
import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.ui.smartimport.internal.GradleProjectConfigurator
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.jdt.core.JavaCore

import java.io.File

class ImporterTest extends Specification {
    IProject testProject
    GradleProjectConfigurator gradleProjectConfigurator
    String testDirectory = "src/test/resources/projects/smart_import/"
    NullProgressMonitor monitor
    IWorkspace workspace

    def setup() {
        monitor = new NullProgressMonitor()
        gradleProjectConfigurator = new GradleProjectConfigurator()
        workspace = ResourcesPlugin.getWorkspace()
        deleteAllWorkspaceProjects(monitor)
    }

    def "Gradle project can be configured"() {
        setup:
        testProject = createAndSetupProject(projectName, projectIsConfigurable, false)

        expect:
        gradleProjectConfigurator.canConfigure(testProject, null, monitor) == projectIsConfigurable

        where:
        projectName << ["GradleProject", "OtherProject"]
        projectIsConfigurable << [true, false]
    }
    
    def "Gradle project is correctly configured"() {
        setup:
        testProject = createAndSetupProject(projectName, isGradleProject, false)

        when:
        configureProject(testProject, gradleProjectConfigurator, monitor)
        
        then:
        testProject.hasNature(GradleProjectNature.ID) == isGradleProject
        testProject.getFile(".settings/gradle.prefs").exists() == isGradleProject

        where:
        projectName << ["GradleProject", "OtherProject"]
        isGradleProject << [true, false]
    }

    def "A Java project is given Java nature"() {
        setup:
        testProject = createAndSetupProject(projectName, isGradleProject, isJavaProject)
        
        when:
        configureProject(testProject, gradleProjectConfigurator, monitor)
        
        then:
        testProject.hasNature(JavaCore.NATURE_ID) == isJavaProject

        where:    
        projectName << ["JavaGradleProject", "NonJavaGradleProject", "OtherProject"]
        isGradleProject << [true, true, false]
        isJavaProject << [true, false, false]
    }
    
    def "Multiproject: All Gradle/Java multiproject build can be imported"() {
        setup:
        copyDirectoriesToWorkspace(testDirectory)
        def parentProject = createAndSetupProject("Project2", false, true)
        
        when:
        configureProject(parentProject, gradleProjectConfigurator, monitor)
        
        then:
        workspace.root.projects.length == 3
        workspace.root.projects.every {project -> project.hasNature(GradleProjectNature.ID) &&
                                                             project.hasNature(JavaCore.NATURE_ID)
        }
    }
    
    def "Multiproject: Non-Gradle parent project, Gradle children not configured"() {
        setup:
        copyDirectoriesToWorkspace(testDirectory)
        def parentProject = createAndSetupProject("Project3", false, false)
        
        when:
        configureProject(parentProject, gradleProjectConfigurator, monitor)
        
        then:
        workspace.root.projects.length == 1
        !workspace.root.projects.every {project -> project.hasNature(GradleProjectNature.ID)}
    }
    
    def cleanup() {
        deleteAllWorkspaceProjects(monitor)
    }
    
    /**
     * Creates and sets up a project.
     * @param projectName The name of the project.
     * @param isGradleProject Whether or not this project should be configured as a Gradle project.
     * @param isJavaProject Whether or not this project should be configured as a Java project.
     * @return The imported project.
     */
    private IProject createAndSetupProject(projectName, isGradleProject, isJavaProject) {
        def project = createTestProject(projectName, workspace, monitor)
        setupProject(project, isGradleProject, isJavaProject)
        project
    }

}
