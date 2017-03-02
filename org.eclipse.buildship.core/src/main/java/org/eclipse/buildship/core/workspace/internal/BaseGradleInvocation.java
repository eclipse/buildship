/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.workspace.GradleInvocation;

/**
 * Common base class for the {@link GradleInvocation} implementations.
 * @author donat
 */
public abstract class BaseGradleInvocation<T extends LongRunningOperation> implements GradleInvocation {

    private final FixedRequestAttributes fixedAttributes;
    private final TransientRequestAttributes transientAttributes;

    public BaseGradleInvocation(FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes) {
        this.fixedAttributes = Preconditions.checkNotNull(fixedAttributes);
        this.transientAttributes = Preconditions.checkNotNull(transientAttributes);
    }

    @Override
    public void run() {
        ProjectConnection connection = null;
        try {
            // apply FixedRequestAttributes on connector and establish TAPI connection
            GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(this.fixedAttributes.getProjectDir());
            GradleDistributionWrapper.from(this.fixedAttributes.getGradleDistribution()).apply(connector);
            connector.useGradleUserHomeDir(this.fixedAttributes.getGradleUserHome());
            connection = connector.connect();

            // apply FixedRequestAttributes on build launcher
            T launcher = create(connection);

            launcher.setJavaHome(this.fixedAttributes.getJavaHome());
            launcher.withArguments(this.fixedAttributes.getArguments());
            launcher.setJvmArguments(this.fixedAttributes.getJvmArguments());

            // transient attributes
            launcher.setStandardOutput(this.transientAttributes.getStandardOutput());
            launcher.setStandardError(this.transientAttributes.getStandardError());
            launcher.setStandardInput(this.transientAttributes.getStandardInput());
            for (ProgressListener listener : this.transientAttributes.getProgressListeners()) {
                launcher.addProgressListener(listener);
            }
            launcher.withCancellationToken(this.transientAttributes.getCancellationToken());

            // execute the build
            launch(launcher);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    protected abstract T create(ProjectConnection connection);

    protected abstract void launch(T launcher);

}
