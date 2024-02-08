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

import java.io.Serializable;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;

public class BuildActionSequence implements BuildAction<Void>, Serializable {

    private static final long serialVersionUID = 1L;
    private final BuildAction<?>[] actions;

    public BuildActionSequence(BuildAction<?> ... actions) {
        super();
        this.actions = actions;
    }

    @Override
    public Void execute(BuildController controller) {
        for (BuildAction<?> action : this.actions) {
            action.execute(controller);
        }
        return null;
    }

}
