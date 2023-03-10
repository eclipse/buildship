/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.launch;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.buildship.core.internal.launch.JavaElementSelection;
import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchSite;

import java.util.Collection;

/**
 * Resolves elements from the Java source editor.
 */
@SuppressWarnings("restriction")
public final class EditorBackedJavaElementSelection extends JavaElementSelection {

    private final IEditorPart editorPart;

    private EditorBackedJavaElementSelection(IEditorPart editorPart) {
        this.editorPart = Preconditions.checkNotNull(editorPart);
    }

    @Override
    protected Collection<IJavaElement> findJavaElements() {
        try {
            ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(this.editorPart.getEditorInput());
            return typeRoot != null ? ImmutableList.of(findSelectedJavaElements(typeRoot)) : ImmutableList.<IJavaElement>of();
        } catch (JavaModelException e) {
            UiPlugin.logger().warn("Failed to find selected method in Java editor.", e);
            return ImmutableList.of();
        }
    }

    private IJavaElement findSelectedJavaElements(ITypeRoot typeRoot) throws JavaModelException {
        IWorkbenchSite editorSite = this.editorPart.getSite();
        if (editorSite != null) {
            ISelectionProvider selectionProvider = editorSite.getSelectionProvider();
            if (selectionProvider != null) {
                ISelection selection = selectionProvider.getSelection();
                if (selection instanceof TextSelection) {
                    ITextSelection textSelection = (ITextSelection) selection;
                    return SelectionConverter.getElementAtOffset(typeRoot, textSelection);
                }
            }
        }
        // if the selected element is not found then fall back to type opened in the editor
        return typeRoot;
    }

    /**
     * Creates a new instance.
     *
     * @param editorPart the target editor reference to resolve the Java elements from
     * @return the new instance
     */
    public static EditorBackedJavaElementSelection from(IEditorPart editorPart) {
        return new EditorBackedJavaElementSelection(editorPart);
    }

}
