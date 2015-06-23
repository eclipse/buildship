package org.eclipse.buildship.ui.smartimport

import java.io.File;

import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.resources.IProjectDescription
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.jobs.IJobManager
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.configuration.GradleProjectNature
import org.eclipse.buildship.ui.smartimport.internal.GradleProjectConfigurator
import org.eclipse.core.resources.IResource
import org.eclipse.core.runtime.NullProgressMonitor

public class ImporterTestUtilities {

    /**
     * Copys the directories in a given location to the current workspace.
     * @param testDirectoryLocation The location of the test directories to copy.
     */
    static void copyDirectoriesToWorkspace(testDirectoryLocation) {
        File srcFolder = new File(testDirectoryLocation)
        File destFolder = new File(ResourcesPlugin.getWorkspace().root.rawLocation.toString())
        
        if (!srcFolder.exists()) {
            println "Source folder does not exist"
        }
        copyFolder(srcFolder, destFolder)
    }
    
    /**
     * Copys the source folder to the destination.
     * @param src The source folder.
     * @param dest The destination folder.
     */
    static void copyFolder(File src, File dest) {
        def files
        
        if (src.isDirectory()) {
            if (!dest.exists()) {
               dest.mkdir()
            }
            files = src.list()
            for (String file : files) {
               File srcFile = new File(src, file)
               File destFile = new File(dest, file)
               copyFolder(srcFile, destFile)
            }
        } else {
            dest.text = src.text
        }
    }
    
    /**
     * Creates and opens a project with the name `projectName`.
     * @param projectName The name of the new project.
     * @param workspace The current workspace.
     * @param monitor A monitor.
     */
    static IProject createTestProject(projectName, IWorkspace workspace, monitor) {
        def project = workspace.root.getProject(projectName)
        def projectDescription = workspace.newProjectDescription(projectName)
        
        project.create(projectDescription, monitor)
        project.open(IResource.NONE, monitor)
    
        project
    }
    
    /**
     * Sets up the given project to be recognized as a Gradle project, and/or a Java project.
     * @param project
     * @param isGradleProject Whether or not this project should be configured as a Gradle project.
     * @param isJavaProject Whether or not this project should be configured as a Java project.
     */
    static void setupProject(project, isGradleProject, isJavaProject) {
        if (isJavaProject) {
            makeJavaProject(project)
        }
        
        if (isGradleProject) {
            makeGradleProject(project)
        }
    }
    
    /**
     * Configures the given project.
     * @param project The given project.
     */
    static void configureProject(project, gradleProjectConfigurator, monitor) {
        
        /* The GradleProjectConfigurator schedules the ProjectImportJob, but does not wait for it.
         * In order to ensure that the project has been successfully set up, this method needs to wait
         * for the ProjectImportJob to complete.
         */
        gradleProjectConfigurator.configure(project, null, monitor)
        waitForJob("ProjectImportJob")
    }
    
    /**
     * Adds the build.gradle file to the given project.
     * @param project The project being tested.
     */
    static void addGradleBuildFile(IProject project) {
        addFileToProject(project, "build.gradle", "apply plugin: 'java'")
    }
    
    /**
     * Produces the resources necessary for the given project to be recognized as a Java project.
     * @param project The project being tested.
     */
    static void makeJavaProject(project) {
        addFileToProject(project, "src/main/java/Test.java", "")
    }
    
    /**
     * Produces the resources necessary for the given project to be recognized as a Gradle project.
     * @param project
     */
    static void makeGradleProject(project) {
        addGradleBuildFile(project)
        addGradleWrapperFiles(project)
    }
    
    /**
     * Adds the Gradle wrapper files to the given project.
     * @param project The given project.
     */
    static void addGradleWrapperFiles(IProject project) {
        addFileToProject(project, "gradlew", "")
        addFileToProject(project, "gradlew.bat", "")
    }
    
    /**
     * Adds a new file to the given project.
     * @param project The project being tested.
     * @param filename The name of the new file. (Or path from root of project).
     * @param fileContents The contents of the new file.
     */
    static void addFileToProject(IProject project, filename, fileContents) {
        File file = new File(project.getLocation().toFile(), filename)
        file.getParentFile().mkdirs()
        file.text = fileContents
        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor())
    }
    
    /**
     * Waits for the job whose corresponding class ends with jobClassName.
     * @param jobClassName The job's corresponding class name.
     */
    static void waitForJob(jobClassName) {
        Job[] jobs = Job.getJobManager().find(null)
        for(Job job : jobs) {
            if (job.getClass().getName().endsWith(jobClassName)) {
                job.join()
            }
        }
    }
    
    /**
     * Deletes all projects in the workspace.
     */
    static void deleteAllWorkspaceProjects(monitor) {
        IProject[] projects = ResourcesPlugin.getWorkspace().root.projects
        for (IProject project : projects) {
            project.delete(true, true, monitor)
        }
    }

}
	