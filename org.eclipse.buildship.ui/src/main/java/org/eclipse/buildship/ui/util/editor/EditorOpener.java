/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz (vogella GmbH) - exposing the class
 *******************************************************************************/

package org.eclipse.buildship.ui.util.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This class has been adapted from the org.eclipse.search.internal.ui.text.EditorOpener and is used
 * to open editors within the IDE and also allows to reuse the editor instance.
 * <p/>
 * Note: this class has been taken from
 * {@code org.eclipse.search.internal.ui.text.EditorOpener}
 * which is available in library {@code org.eclipse.search:3.10.0}.
 */
public final class EditorOpener {

    private IEditorReference fReusedEditor;

    public IEditorPart open(IWorkbenchPage page, IFile file, boolean reuseEditor, boolean activate) throws PartInitException {
        String editorId;
        IEditorDescriptor desc = IDE.getEditorDescriptor(file);
        if (desc == null) {
            editorId = PlatformUI.getWorkbench().getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID).getId();
        } else {
            editorId = desc.getId();
        }

        if (reuseEditor) {
            return showWithReuse(page, file, editorId, activate);
        } else {
            return showWithoutReuse(page, file, editorId, activate);
        }
    }

    public IEditorPart openAndSelect(IWorkbenchPage page, IFile file, boolean reuseEditor, boolean activate) throws PartInitException {
        String editorId;
        IEditorDescriptor desc = IDE.getEditorDescriptor(file);
        if (desc == null || !desc.isInternal()) {
            editorId = "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
        } else {
            editorId = desc.getId();
        }

        IEditorPart editor;
        if (reuseEditor) {
            editor = showWithReuse(page, file, editorId, activate);
        } else {
            editor = showWithoutReuse(page, file, editorId, activate);
        }
        return editor;
    }

    public IEditorPart openAndSelect(IWorkbenchPage page, IFile file, int offset, int length, boolean reuseEditor, boolean activate) throws PartInitException {
        IEditorPart editor = openAndSelect(page, file, reuseEditor, activate);
        if (editor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editor;
            textEditor.selectAndReveal(offset, length);
        } else if (editor != null) {
            showWithMarker(editor, file, offset, length);
        }
        return editor;
    }

    private IEditorPart showWithReuse(IWorkbenchPage page, IFile file, String editorId, boolean activate) throws PartInitException {
        IEditorInput input = new FileEditorInput(file);
        IEditorPart editor = page.findEditor(input);
        if (editor != null) {
            page.bringToTop(editor);
            if (activate) {
                page.activate(editor);
            }
            return editor;
        }

        IEditorReference reusedEditorRef = this.fReusedEditor;
        if (reusedEditorRef != null) {
            boolean isOpen = reusedEditorRef.getEditor(false) != null;
            boolean canBeReused = isOpen && !reusedEditorRef.isDirty() && !reusedEditorRef.isPinned();
            if (canBeReused) {
                boolean showsSameInputType = reusedEditorRef.getId().equals(editorId);
                if (!showsSameInputType) {
                    page.closeEditors(new IEditorReference[]{reusedEditorRef}, false);
                    this.fReusedEditor = null;
                } else {
                    editor = reusedEditorRef.getEditor(true);
                    if (editor instanceof IReusableEditor) {
                        ((IReusableEditor) editor).setInput(input);
                        page.bringToTop(editor);
                        if (activate) {
                            page.activate(editor);
                        }
                        return editor;
                    }
                }
            }
        }

        editor = page.openEditor(input, editorId, activate);
        if (editor instanceof IReusableEditor) {
            this.fReusedEditor = (IEditorReference) page.getReference(editor);
        } else {
            this.fReusedEditor = null;
        }
        return editor;
    }

    private IEditorPart showWithoutReuse(IWorkbenchPage page, IFile file, String editorId, boolean activate) throws PartInitException {
        return IDE.openEditor(page, file, editorId, activate);
    }

    private void showWithMarker(IEditorPart editor, IFile file, int offset, int length) throws PartInitException {
        IMarker marker = null;
        try {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            marker = file.createMarker(bundle.getSymbolicName() + "navigationmarker"); //$NON-NLS-1$
            Map<String, Integer> attributes = new HashMap<String, Integer>(4);
            attributes.put(IMarker.CHAR_START, offset);
            attributes.put(IMarker.CHAR_END, offset + length);
            marker.setAttributes(attributes);
            IDE.gotoMarker(editor, marker);
        } catch (CoreException e) {
            throw new PartInitException(e.getMessage(), e);
        } finally {
            if (marker != null) {
                try {
                    marker.delete();
                } catch (CoreException e) {
                    // ignore
                }
            }
        }
    }

}
