/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.progress;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnector;

import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Job that belongs to the Gradle job family.
 */
public abstract class GradleJob extends Job {

    private final CancellationTokenSource tokenSource = GradleConnector.newCancellationTokenSource();

    public GradleJob(String name) {
        super(name);
    }

    protected CancellationToken getToken() {
        return this.tokenSource.token();
    }

    @Override
    public boolean belongsTo(Object family) {
        return CorePlugin.GRADLE_JOB_FAMILY.equals(family);
    }

    @Override
    protected void canceling() {
        this.tokenSource.cancel();
    }
}
