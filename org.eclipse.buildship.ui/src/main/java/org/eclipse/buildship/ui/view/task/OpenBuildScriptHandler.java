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

package org.eclipse.buildship.ui.view.task;

import java.io.File;

import com.google.common.base.Optional;

import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.buildship.core.omnimodel.OmniGradleScript;
import org.eclipse.buildship.core.util.gradle.Maybe;
import org.eclipse.buildship.ui.util.editor.EditorUtils;
import org.eclipse.buildship.ui.util.nodeselection.NodeSelection;

/**
 * Opens the build file for the selected {@link ProjectNode}.
 */
public final class OpenBuildScriptHandler extends SelectionDependentHandler {

    @Override
    protected boolean isEnabledFor(NodeSelection selection) {
        return OpenBuildScriptAction.isEnabledForSelection(selection);
    }

    @Override
    public Object execute(ExecutionEvent event) {
        NodeSelection selectionHistory = getSelectionHistory(event);
        for (ProjectNode projectNode : selectionHistory.toList(ProjectNode.class)) {
            Optional<File> buildScript = getBuildScriptFor(projectNode);
            if (buildScript.isPresent()) {
                EditorUtils.openInInternalEditor(buildScript.get(), true);
            }
        }
        return null;
    }

    private Optional<File> getBuildScriptFor(ProjectNode projectNode) {
        Maybe<OmniGradleScript> buildScript = projectNode.getGradleProject().getBuildScript();
        return buildScript.isPresent() ? Optional.fromNullable(buildScript.get().getSourceFile()) : Optional.<File>absent();
    }

}
