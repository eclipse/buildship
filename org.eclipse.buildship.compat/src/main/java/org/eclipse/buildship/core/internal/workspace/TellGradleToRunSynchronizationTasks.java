/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

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
