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

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class has been adapted from the org.eclipse.search.internal.ui.text.EditorOpener and is used
 * to open editors within the IDE and also allows to reuse the editor instance.
 *
 */
public final class EditorOpener {

	private IEditorReference fReusedEditor;

    public IEditorPart open(IWorkbenchPage wbPage, IFile file, boolean reuseEditor, boolean activate) throws PartInitException {
        if (reuseEditor) {
            return showWithReuse(file, wbPage, getEditorID(file), activate);
        }
		return showWithoutReuse(file, wbPage, getEditorID(file), activate);
	}

    public IEditorPart openAndSelect(IWorkbenchPage wbPage, IFile file, int offset, int length, boolean reuseEditor, boolean activate) throws PartInitException {
		String editorId;
		IEditorDescriptor desc= IDE.getEditorDescriptor(file);
		if (desc == null || !desc.isInternal()) {
			editorId= "org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		} else {
			editorId= desc.getId();
		}

		IEditorPart editor;
        if (reuseEditor) {
			editor= showWithReuse(file, wbPage, editorId, activate);
		} else {
			editor= showWithoutReuse(file, wbPage, editorId, activate);
		}

		if (editor instanceof ITextEditor) {
			ITextEditor textEditor= (ITextEditor) editor;
			textEditor.selectAndReveal(offset, length);
		} else if (editor != null) {
			showWithMarker(editor, file, offset, length);
		}
		return editor;
	}

	private IEditorPart showWithoutReuse(IFile file, IWorkbenchPage wbPage, String editorID, boolean activate) throws PartInitException {
		return IDE.openEditor(wbPage, file, editorID, activate);
	}


	private String getEditorID(IFile file) throws PartInitException {
		IEditorDescriptor desc= IDE.getEditorDescriptor(file);
		if (desc == null) {
            return PlatformUI.getWorkbench().getEditorRegistry().findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID).getId();
        }
		return desc.getId();
	}

	private IEditorPart showWithReuse(IFile file, IWorkbenchPage page, String editorId, boolean activate) throws PartInitException {
		IEditorInput input= new FileEditorInput(file);
		IEditorPart editor= page.findEditor(input);
		if (editor != null) {
			page.bringToTop(editor);
			if (activate) {
				page.activate(editor);
			}
			return editor;
		}
		IEditorReference reusedEditorRef= this.fReusedEditor;
		if (reusedEditorRef !=  null) {
			boolean isOpen= reusedEditorRef.getEditor(false) != null;
			boolean canBeReused= isOpen && !reusedEditorRef.isDirty() && !reusedEditorRef.isPinned();
			if (canBeReused) {
				boolean showsSameInputType= reusedEditorRef.getId().equals(editorId);
				if (!showsSameInputType) {
					page.closeEditors(new IEditorReference[] { reusedEditorRef }, false);
                    this.fReusedEditor = null;
				} else {
					editor= reusedEditorRef.getEditor(true);
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
		editor= page.openEditor(input, editorId, activate);
		if (editor instanceof IReusableEditor) {
            this.fReusedEditor = (IEditorReference) page.getReference(editor);
		} else {
            this.fReusedEditor = null;
		}
		return editor;
	}

	private void showWithMarker(IEditorPart editor, IFile file, int offset, int length) throws PartInitException {
		IMarker marker= null;
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
