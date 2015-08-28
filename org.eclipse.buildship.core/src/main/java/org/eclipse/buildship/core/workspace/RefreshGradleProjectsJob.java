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

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import org.eclipse.buildship.core.AggregateException;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.util.predicate.Predicates;
import org.eclipse.buildship.core.util.progress.ToolingApiJob;

/**
 * Finds the root projects for the selection and starts a {@link RefreshGradleProjectJob} on each of
 * them.
 */
public final class RefreshGradleProjectsJob extends ToolingApiJob {

    // todo (etst) we should also support removing and adding projects during the refresh

    private final List<IProject> projects;

    public RefreshGradleProjectsJob(List<IProject> projects) {
        super("Refresh Gradle projects", true);
        this.projects = ImmutableList.copyOf(projects);
    }

    @Override
    protected void runToolingApiJob(final IProgressMonitor monitor) throws Exception {
        Set<FixedRequestAttributes> requestAttributes = getUniqueRootAttributes(this.projects);
        monitor.beginTask("Refresh selected Gradle projects in workspace", requestAttributes.size());
        final List<Exception> exceptions = new CopyOnWriteArrayList<Exception>();
        try {
            final CountDownLatch latch = new CountDownLatch(requestAttributes.size());
            for (final FixedRequestAttributes attributes : requestAttributes) {
                Job refreshjob = new RefreshGradleProjectJob(attributes);
                refreshjob.addJobChangeListener(new JobChangeAdapter() {

                    @Override
                    public void done(IJobChangeEvent event) {
                        if (event.getResult().getException() != null && event.getResult().getException() instanceof Exception) {
                            exceptions.add((Exception) event.getResult().getException());
                        }
                        monitor.worked(1);
                        latch.countDown();
                    };
                });
                refreshjob.schedule();
            }
            latch.await();
        } finally {
            monitor.done();
            rethrowExceptionsIfAny(exceptions);
        }
    }

    @Override
    protected void canceling() {
        // cancel all running RefreshGradleProjectJob instances
        Job.getJobManager().cancel(RefreshGradleProjectJob.class.getName());
    }

    private static void rethrowExceptionsIfAny(List<Exception> exceptions) throws Exception {
        if (exceptions.size() == 1) {
            throw exceptions.get(0);
        } else if (exceptions.size() > 1) {
            throw new AggregateException(exceptions);
        }
    }

    private static Set<FixedRequestAttributes> getUniqueRootAttributes(List<IProject> projects) {
        return FluentIterable.from(projects).filter(Predicates.accessibleGradleProject()).transform(new Function<IProject, FixedRequestAttributes>() {

            @Override
            public FixedRequestAttributes apply(IProject project) {
                return CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();
            }
        }).toSet();
    }

}
