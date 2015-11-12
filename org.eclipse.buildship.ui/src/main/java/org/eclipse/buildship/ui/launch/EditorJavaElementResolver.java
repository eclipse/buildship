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

package org.eclipse.buildship.ui.launch;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

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

import org.eclipse.buildship.ui.UiPlugin;

/**
 * Resolves elements from the Java source editor.
 */
@SuppressWarnings("restriction")
public final class EditorJavaElementResolver extends JavaElementResolver {

    private final IEditorPart editorPart;

    public EditorJavaElementResolver(IEditorPart editorPart) {
        this.editorPart = editorPart;
    }

    @Override
    public Collection<IJavaElement> findJavaElements() {
        try {
            ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(this.editorPart.getEditorInput());
            if (typeRoot == null) {
                return ImmutableList.of();
            } else {
                return ImmutableList.of(findSelectedJavaElements(typeRoot));
            }
        } catch (JavaModelException e) {
            UiPlugin.logger().warn("Failed find selected method in Java editor", e);
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
        // if the selected method is not found, then return the target type root
        return typeRoot;
    }

    /**
     * Creates a new instance.
     *
     * @param editorPart the target editor reference to resolve the Java elements from
     * @return the new instance
     */
    public static EditorJavaElementResolver from(IEditorPart editorPart) {
        return new EditorJavaElementResolver(editorPart);
    }

}
