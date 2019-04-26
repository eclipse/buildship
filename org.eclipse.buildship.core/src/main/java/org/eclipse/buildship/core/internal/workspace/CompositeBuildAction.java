/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.internal.workspace;

import java.io.Serializable;
import java.util.List;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildController;

public class CompositeBuildAction implements BuildAction<Void>, Serializable {

    private final List<BuildAction<Void>> actions;

    public CompositeBuildAction(List<BuildAction<Void>> actions) {
        super();
        this.actions = actions;
    }

    @Override
    public Void execute(BuildController controller) {
        for (BuildAction<Void> action : this.actions) {
            action.execute(controller);
        }
        return null;
    }

}
