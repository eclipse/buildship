package org.eclipse.buildship.core.workspace;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.OmniEclipseGradleBuild;
import com.gradleware.tooling.toolingmodel.OmniEclipseProject;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.console.ProcessStreams;
import org.eclipse.buildship.core.gradle.Specs;
import org.eclipse.buildship.core.util.progress.DelegatingProgressListener;
import org.eclipse.buildship.core.util.progress.ToolingApiWorkspaceJob;
import org.eclipse.buildship.core.workspace.internal.ClasspathContainerUpdater;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.ProgressListener;

import java.util.List;

/**
* Refreshes an Eclipse Java project's Gradle specific parts.
*/
public final class RefreshJavaWorkspaceProjectJob extends ToolingApiWorkspaceJob {

    private final IJavaProject project;

    public RefreshJavaWorkspaceProjectJob(IJavaProject project) {
        super("Initialize Gradle classpath for project '" + project.getElementName() + "'", false);
        this.project = project;
    }

    @Override
    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
        monitor.beginTask("Initializing classpath", 100);

        // use the same rule as the ProjectImportJob to do the initialization
        IJobManager manager = Job.getJobManager();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        manager.beginRule(workspaceRoot, monitor);
        try {
            internalInitialize(this.project, monitor, getToken());
        } finally {
            manager.endRule(workspaceRoot);
        }

        // monitor is closed by caller in super class
    }

    private void internalInitialize(IJavaProject javaProject, IProgressMonitor monitor, CancellationToken token) throws CoreException {
        IProject project = javaProject.getProject();
        if (GradleProjectNature.INSTANCE.isPresentOn(project)) {
            Optional<OmniEclipseProject> gradleProject = findEclipseProject(project, monitor, token);
            monitor.worked(70);
            if (gradleProject.isPresent()) {
                if (project.isAccessible()) {
                    CorePlugin.workspaceGradleOperations().updateProjectInWorkspace(project, gradleProject.get(), monitor);
                }
            } else {
                throw new GradlePluginsRuntimeException(String.format("Cannot find Eclipse project model for project %s.", project));
            }
        } else {
            // update project/external dependencies to be empty
            ClasspathContainerUpdater.clear(javaProject, new SubProgressMonitor(monitor, 100));
        }
    }

    private Optional<OmniEclipseProject> findEclipseProject(IProject project, IProgressMonitor monitor, CancellationToken token) {
        ProjectConfiguration configuration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
        OmniEclipseGradleBuild eclipseGradleBuild = fetchEclipseGradleBuild(configuration.getRequestAttributes(), monitor, token);
        return eclipseGradleBuild.getRootEclipseProject().tryFind(Specs.eclipseProjectMatchesProjectPath(configuration.getProjectPath()));
    }

    private OmniEclipseGradleBuild fetchEclipseGradleBuild(FixedRequestAttributes fixedRequestAttributes, IProgressMonitor monitor, CancellationToken token) {
        ProcessStreams streams = CorePlugin.processStreamsProvider().getBackgroundJobProcessStreams();
        List<ProgressListener> progressListeners = ImmutableList.<ProgressListener>of(new DelegatingProgressListener(monitor));
        TransientRequestAttributes transientAttributes = new TransientRequestAttributes(false, streams.getOutput(), streams.getError(), null, progressListeners,
                ImmutableList.<org.gradle.tooling.events.ProgressListener>of(), token);
        ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedRequestAttributes);
        return repository.fetchEclipseGradleBuild(transientAttributes, FetchStrategy.LOAD_IF_NOT_CACHED);
    }

}
