/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.task;

import java.io.File;

import org.gradle.tooling.model.gradle.GradleScript;

import com.google.common.base.Optional;

import org.eclipse.core.commands.ExecutionEvent;

import org.eclipse.buildship.ui.internal.util.editor.EditorUtils;
import org.eclipse.buildship.ui.internal.util.nodeselection.NodeSelection;

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
        GradleScript buildScript = projectNode.getGradleProject().getBuildScript();
        return buildScript != null ? Optional.fromNullable(buildScript.getSourceFile()) : Optional.<File>absent();
    }

}
