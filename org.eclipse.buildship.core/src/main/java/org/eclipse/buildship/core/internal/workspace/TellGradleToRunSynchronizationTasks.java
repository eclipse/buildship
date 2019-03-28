/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.internal.workspace;


import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;
import org.gradle.tooling.model.eclipse.RunEclipseSynchronizationTasks;

public final class TellGradleToRunSynchronizationTasks implements BuildAction<Void> {

    @Override
    public Void execute(BuildController controller) {
        controller.getModel(RunEclipseSynchronizationTasks.class);
        return null;
    }

}
