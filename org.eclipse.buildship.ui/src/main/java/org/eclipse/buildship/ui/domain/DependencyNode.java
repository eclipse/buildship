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

package org.eclipse.buildship.ui.domain;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.gradleware.tooling.toolingmodel.OmniExternalDependency;

/**
 * Domain object representing a Gradle dependency.
 * <p/>
 * It holds a reference to a {@link OmniExternalDependency} and is aware of the parent containing
 * this object.
 * <p/>
 * This object is presented in various places in the UI.
 */
public final class DependencyNode {

    private final ProjectNode parentProjectNode;
    private final OmniExternalDependency dependency;

    public DependencyNode(ProjectNode parentProjectNode, OmniExternalDependency dependency) {
        this.parentProjectNode = Preconditions.checkNotNull(parentProjectNode);
        this.dependency = Preconditions.checkNotNull(dependency);
    }

    public ProjectNode getParentProjectNode() {
        return this.parentProjectNode;
    }

    public OmniExternalDependency getDependency() {
        return this.dependency;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DependencyNode that = (DependencyNode) other;
        return Objects.equal(this.parentProjectNode, that.parentProjectNode) && Objects.equal(this.dependency, that.dependency);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.parentProjectNode, this.dependency);
    }

}
