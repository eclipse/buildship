/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.core.launch.internal;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.Request;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.launch.ExecuteLaunchRequestEvent;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;

/**
 * Default implementation of {@link ExecuteLaunchRequestEvent}.
 */
public final class DefaultExecuteLaunchRequestEvent implements ExecuteLaunchRequestEvent {

    private final Job job;
    private final Request<Void> request;
    private final GradleRunConfigurationAttributes runConfigurationAttributes;
    private final String processName;
    private ProcessDescription processDescription;

    public DefaultExecuteLaunchRequestEvent(Job job, Request<Void> request, GradleRunConfigurationAttributes runConfigurationAttributes, String processName, ProcessDescription processDescription) {
        this.job = job;
        this.request = Preconditions.checkNotNull(request);
        this.runConfigurationAttributes = Preconditions.checkNotNull(runConfigurationAttributes);
        this.processName = Preconditions.checkNotNull(processName);
        this.processDescription = processDescription;
    }

    @Override
    public Job getJob() {
        return this.job;
    }

    @Override
    public Request<Void> getRequest() {
        return this.request;
    }

    @Override
    public GradleRunConfigurationAttributes getRunConfigurationAttributes() {
        return this.runConfigurationAttributes;
    }

    @Override
    public String getProcessName() {
        return this.processName;
    }

    @Override
    public ProcessDescription getProcessDescription() {
        return this.processDescription;
    }
}
