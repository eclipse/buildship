/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.extension;

import java.util.function.Function;

import org.gradle.tooling.ProjectConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Describes a valid or invalid contributed build action.
 *
 * @author Donat Csikos
 */
public final class BuildActionContribution {

    private final IConfigurationElement extension;
    private final String id;
    private final String pluginId; // TODO (donat) we could extract this to a common base-class
    private Function<ProjectConnection, ?> buildAction;

    private BuildActionContribution(IConfigurationElement extension, String id, String pluginId) {
        this.extension = extension;
        this.id = id;
        this.pluginId = pluginId;
    }

    public String getId() {
        return this.id;
    }

    @SuppressWarnings("unchecked")
    public Function<ProjectConnection, ?> createBuildAction() throws CoreException {
        if (this.buildAction == null) {
            try {
                this.buildAction = (Function<ProjectConnection, ?>) this.extension.createExecutableExtension("class");
            } catch (ClassCastException e) {
                throw new CoreException(new Status(IStatus.WARNING, this.pluginId, "Contributed class cannot be cast to 'Function<ProjectConnection, ?>'", e));
            }
        }
        return this.buildAction;
    }

    public static BuildActionContribution from(IConfigurationElement extension) {
        String id = extension.getAttribute("id");
        String pluginId = extension.getContributor().getName();
        return new BuildActionContribution(extension, id, pluginId);
    }
}
