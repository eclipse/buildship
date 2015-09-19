/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.workspace;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import org.eclipse.buildship.core.AggregateException;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.predicate.Predicates;
import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.util.progress.ToolingApiCommand;
import org.eclipse.buildship.core.util.progress.ToolingApiInvoker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Finds the Gradle root projects for the given set of Eclipse projects and for each found
 * Gradle root project, it synchronizes the Eclipse workspace via {@link SynchronizeGradleProjectJob}.
 */
public final class SynchronizeGradleProjectsJob extends Job {

    private final List<IProject> projects;

    public SynchronizeGradleProjectsJob(List<IProject> projects) {
        super("Refresh Gradle projects");
        this.projects = ImmutableList.copyOf(projects);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        ToolingApiInvoker invoker = new ToolingApiInvoker(getName(), true);
        return invoker.invoke(new ToolingApiCommand() {
            @Override
            public void run() throws Throwable {
                scheduleRefreshJobs(monitor);
            }
        }, monitor);
    }

    private void scheduleRefreshJobs(final IProgressMonitor monitor) throws Throwable {
        // find all the unique root projects for the given list of projects and
        // reload the workspace project configuration for each of them (incl. their respective child projects)
        Set<FixedRequestAttributes> rootRequestAttributes = getUniqueRootAttributes(this.projects);
        monitor.beginTask("Refresh selected Gradle projects in workspace", rootRequestAttributes.size());
        final List<Throwable> errors = new CopyOnWriteArrayList<Throwable>();
        try {
            final CountDownLatch latch = new CountDownLatch(rootRequestAttributes.size());
            for (FixedRequestAttributes requestAttributes : rootRequestAttributes) {
                Job refreshJob = new SynchronizeGradleProjectJob(requestAttributes, ImmutableList.<String>of(), AsyncHandler.NO_OP);
                refreshJob.addJobChangeListener(new JobChangeAdapter() {

                    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
                    @Override
                    public void done(IJobChangeEvent event) {
                        if (!event.getResult().isOK()) {
                            Throwable error = event.getResult().getException();
                            if (error != null) {
                                errors.add(error);
                            }
                        }
                        monitor.worked(1);
                        latch.countDown();
                    }
                });
                refreshJob.schedule();
            }
            latch.await();
        } finally {
            monitor.done();
            rethrowExceptionsIfAny(errors);
        }
    }

    private Set<FixedRequestAttributes> getUniqueRootAttributes(List<IProject> projects) {
        return FluentIterable.from(projects).filter(Predicates.accessibleGradleProject()).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

    private void rethrowExceptionsIfAny(List<Throwable> errors) throws Throwable {
        if (errors.size() == 1) {
            throw errors.get(0);
        } else if (errors.size() > 1) {
            throw new AggregateException(errors);
        }
    }

    @Override
    protected void canceling() {
        // cancel all running SynchronizeGradleProjectJob instances
        Job.getJobManager().cancel(SynchronizeGradleProjectJob.class.getName());
    }

}
