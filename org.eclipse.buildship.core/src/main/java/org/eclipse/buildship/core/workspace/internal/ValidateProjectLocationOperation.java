/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.UnsupportedConfigurationException;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProject;

/**
 * Verifies that none of the modules are located in the Eclipse workspace root.
 *
 * @author Donat Csikos
 */
public class ValidateProjectLocationOperation {

    private static final File WORKSPACE_ROOT = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

    private final Set<OmniEclipseProject> projects;

    public ValidateProjectLocationOperation(Set<OmniEclipseProject> projects) {
        this.projects = ImmutableSet.copyOf(projects);
    }

    public void run(IProgressMonitor monitor) {
        for (OmniEclipseProject project : this.projects) {
            if (project.getProjectDirectory().equals(WORKSPACE_ROOT)) {
                throw new UnsupportedConfigurationException(String.format("Project %s location matches workspace root %s", project.getName(), WORKSPACE_ROOT.getAbsolutePath()));
            }
        }

    }

}
