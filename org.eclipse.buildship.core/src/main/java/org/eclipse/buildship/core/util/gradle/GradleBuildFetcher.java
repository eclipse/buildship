/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Ian Stewart-Binks (Red Hat, Inc.) - Bug 469011: Show actual values during a Gradle build in the command line
 */

package org.eclipse.buildship.core.util.gradle;

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment;
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure;
import com.gradleware.tooling.toolingmodel.repository.FetchStrategy;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.ModelRepository;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Fetches the Gradle build structure and environment.
 */
public class GradleBuildFetcher {

    public static OmniBuildEnvironment fetchBuildEnvironment(IProgressMonitor monitor, TransientRequestAttributes transientAttributes, FixedRequestAttributes fixedAttributes) {
        monitor.beginTask("Load Gradle Build Environment", IProgressMonitor.UNKNOWN);
        try {
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedAttributes);
            return repository.fetchBuildEnvironment(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

    public static OmniGradleBuildStructure fetchGradleBuildStructure(IProgressMonitor monitor, TransientRequestAttributes transientAttributes, FixedRequestAttributes fixedAttributes) {
        monitor.beginTask("Load Gradle Project Structure", IProgressMonitor.UNKNOWN);
        try {
            ModelRepository repository = CorePlugin.modelRepositoryProvider().getModelRepository(fixedAttributes);
            return repository.fetchGradleBuildStructure(transientAttributes, FetchStrategy.FORCE_RELOAD);
        } finally {
            monitor.done();
        }
    }

}
