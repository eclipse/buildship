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

package org.eclipse.buildship.ui.util.editor;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import java.io.File;

/**
 * Contains helper methods related to interacting with the Eclipse editors.
 */
public final class EditorUtils {

    private EditorUtils() {
    }

    public static void openInInternalEditor(File file, boolean activate) {
        String editorId;
        IEditorDescriptor desc = getEditorDescriptor(file);
        if (desc == null || !desc.isInternal()) {
            editorId = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
        } else {
            editorId = desc.getId();
        }

        try {
            IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IDE.openEditor(activePage, file.toURI(), editorId, activate);
        } catch (PartInitException e) {
            String message = String.format("Cannot open file %s in editor.", file.getAbsolutePath());
            UiPlugin.logger().error(message, e); //$NON-NLS-1$
        }
    }

    private static IEditorDescriptor getEditorDescriptor(File buildScript) {
        try {
            return IDE.getEditorDescriptor(buildScript.getName());
        } catch (PartInitException e) {
            // thrown if no editor can be found
            return null;
        }
    }

}
