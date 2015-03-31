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

import java.io.File;

import com.google.common.base.Optional;

import com.gradleware.tooling.toolingmodel.OmniGradleScript;
import com.gradleware.tooling.toolingmodel.util.Maybe;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.generic.NodeSelection;

/**
 * Opens the build file for the selected {@link ProjectNode}.
 */
public final class OpenBuildScriptHandler extends SelectionDependentHandler {

    @Override
    protected boolean isEnabledFor(NodeSelection selection) {
        // todo (etst) avoid duplication between here and code in the action
        return selection.hasAllNodesOfType(ProjectNode.class) && selection.isSingleSelection();
    }

    @Override
    public Object execute(ExecutionEvent event) {
        NodeSelection selectionHistory = getSelectionHistory(event);
        ProjectNode projectNode = selectionHistory.getFirstNode(ProjectNode.class);
        Optional<File> buildScript = getBuildScriptFor(projectNode);
        if (buildScript.isPresent()) {
            IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
            openBuildScript(page, buildScript.get());
        } else {
            // todo (etst) show dialog that there is no script available for the given project
        }

        return null;
    }

    private Optional<File> getBuildScriptFor(ProjectNode projectNode) {
        Maybe<OmniGradleScript> buildScript = projectNode.getGradleProject().getBuildScript();
        return buildScript.isPresent() ? Optional.fromNullable(buildScript.get().getSourceFile()) : Optional.<File>absent();
    }

    private void openBuildScript(IWorkbenchPage page, File buildScript) {
        IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
        IEditorDescriptor editor = editorReg.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID);

        try {
            IDE.openEditor(page, buildScript.toURI(), editor.getId(), true);
        } catch (PartInitException e) {
            // todo (etst) better error handling
            UiPlugin.logger().error("Can't open build file", e); //$NON-NLS-1$
        }
    }

}
