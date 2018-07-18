package org.eclipse.buildship;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.util.gradle.GradleDistribution;

public interface GradleWorkspace {

    BuildIdentifier newBuildIdentifier(File projectLocation);

    BuildIdentifier newBuildIdentifier(File projectLocation, GradleDistribution gradleDistribution);

    void performImport(BuildIdentifier id, IProgressMonitor monitor, Class<? extends GradleProjectConfigurator> configuratorClass) throws GradleException;

    /*
    BuildIdentifier newBuildIdentifier(File projectLocation, GradleDistribution gradleDistribution);

    void performImport(BuildIdentifier id, IProgressMonitor monitor) throws GradleException;

    void performImport(BuildIdentifier id, IProgressMonitor monitor, Class<? extends GradleProjectConfigurator>... configuratorClass) throws GradleException;

    Collection<Task> collectTasks(BuildIdentifier id) throws GradleException;

    public interface Task {
        void execute();
    }
    */
}
