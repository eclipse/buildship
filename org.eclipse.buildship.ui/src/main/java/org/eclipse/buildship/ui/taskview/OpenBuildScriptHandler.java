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

package org.eclipse.buildship.ui.taskview;

import com.google.common.base.Optional;
import com.gradleware.tooling.toolingmodel.OmniGradleScript;
import com.gradleware.tooling.toolingmodel.util.Maybe;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import java.io.File;

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
        for (ProjectNode projectNode : selectionHistory.getNodes(ProjectNode.class)) {
            Optional<File> buildScript = getBuildScriptFor(projectNode);
            if (buildScript.isPresent()) {
                IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
                openBuildScript(page, buildScript.get());
            }
        }
        return null;
    }

    private Optional<File> getBuildScriptFor(ProjectNode projectNode) {
        Maybe<OmniGradleScript> buildScript = projectNode.getGradleProject().getBuildScript();
        return buildScript.isPresent() ? Optional.fromNullable(buildScript.get().getSourceFile()) : Optional.<File>absent();
    }

    private void openBuildScript(IWorkbenchPage page, File buildScript) {
        String editorId;
        IEditorDescriptor desc = getEditorDescriptor(buildScript);
        if (desc == null || !desc.isInternal()) {
            editorId = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
        } else {
            editorId = desc.getId();
        }

        try {
            IDE.openEditor(page, buildScript.toURI(), editorId, true);
        } catch (PartInitException e) {
            UiPlugin.logger().error(String.format("Cannot open Gradle build file %s.", buildScript.getAbsolutePath()), e); //$NON-NLS-1$        }
        }
    }

    private IEditorDescriptor getEditorDescriptor(File buildScript) {
        try {
            return IDE.getEditorDescriptor(buildScript.getName());
        } catch (PartInitException e) {
            // thrown if no editor can be found
            return null;
        }
    }

}
