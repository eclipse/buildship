/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.view.execution;

import java.util.Collection;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.buildship.ui.UiPlugin;

/**
 * This {@link IResourceVisitor} looks up IResources in the workspace, which fit to the given
 * qualifiedClassName and opens fitting resources in an editor.
 *
 */
public class TestResourceVisitor implements IResourceVisitor {

    private static final String BIN_FOLDER_NAME = "bin"; //$NON-NLS-1$

    private Display display;
    private String className;
    private String methodName;
    private Collection<String> fileExtensions;

    public TestResourceVisitor(Display display, String qualifiedClassName, String methodName, Collection<String> fileExtensions) {
        this.display = display;
        this.className = qualifiedClassName;
        this.methodName = methodName;
        this.fileExtensions = fileExtensions;
    }

    @Override
    public boolean visit(final IResource resource) throws CoreException {
        if (resource.getType() == IResource.FILE && this.fileExtensions.contains(resource.getFileExtension())) {
            // map dots of qualified className to resource separators
            String classNameToPath = this.className.replaceAll(Pattern.quote("."), "/");
            String projectRelativePath = resource.getProjectRelativePath().toString();
            // ignore resources in the bin folder and find out whether the path of the resource fits
            // to the given class name
            if (!projectRelativePath.startsWith(BIN_FOLDER_NAME) && projectRelativePath.contains(classNameToPath)) {
                this.display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IFile file = resource.getAdapter(IFile.class);
                        if (file != null) {
                            try {
                                IEditorPart openEditor = IDE.openEditor(activePage, file);
                                selectClassOrMethodInEditor(openEditor);
                            } catch (PartInitException e) {
                                UiPlugin.logger().error(e.getMessage(), e);
                            } catch (BadLocationException e) {
                                UiPlugin.logger().error(e.getMessage(), e);
                            }
                        }
                    }

                    private void selectClassOrMethodInEditor(IEditorPart openEditor) throws BadLocationException {
                        if (openEditor instanceof ITextEditor) {
                            ITextEditor textEditor = (ITextEditor) openEditor;
                            IDocumentProvider documentProvider = textEditor.getDocumentProvider();
                            IDocument document = documentProvider.getDocument(textEditor.getEditorInput());

                            FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
                            String methodOrClass = TestResourceVisitor.this.methodName != null ? TestResourceVisitor.this.methodName : Files.getNameWithoutExtension(resource.getName());
                            IRegion find = findReplaceDocumentAdapter.find(0, methodOrClass, true, true, false, false);
                            if (find != null) {
                                textEditor.selectAndReveal(find.getOffset(), find.getLength());
                            }
                        }
                    }
                });
                return false;
            }
        }
        return true;
    }

}
