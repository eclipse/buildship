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

import com.google.common.collect.ImmutableSet;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.buildship.core.util.progress.AsyncHandler;

/**
 * For STS, don't remove!
 * @author Stefan Oehme
 */
public final class SynchronizeGradleProjectJob extends org.eclipse.buildship.core.workspace.internal.SynchronizeGradleBuildsJob {

    public SynchronizeGradleProjectJob(FixedRequestAttributes rootRequestAttributes, List<String> unused, AsyncHandler initializer) {
        super(ImmutableSet.of(rootRequestAttributes), NewProjectHandler.IMPORT_AND_MERGE, initializer);
    }

}
