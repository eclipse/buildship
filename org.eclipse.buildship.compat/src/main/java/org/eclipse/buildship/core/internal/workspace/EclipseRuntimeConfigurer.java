/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace;

import java.io.Serializable;
import java.util.Objects;

import org.gradle.api.Action;
import org.gradle.tooling.model.eclipse.EclipseRuntime;
import org.gradle.tooling.model.eclipse.EclipseWorkspace;

public class EclipseRuntimeConfigurer implements Action<EclipseRuntime>, Serializable {

    private static final long serialVersionUID = 1L;
    private final EclipseWorkspace workspace;

    public EclipseRuntimeConfigurer(EclipseWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void execute(EclipseRuntime t) {
        t.setWorkspace(this.workspace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.workspace);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EclipseRuntimeConfigurer other = (EclipseRuntimeConfigurer) obj;
        return Objects.equals(this.workspace, other.workspace);
    }
}
