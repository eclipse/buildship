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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.util.editor.EditorOpener;

/**
 * This {@link IResourceVisitor} looks up IResources in the workspace, which fit to the given
 * qualifiedClassName and opens fitting resources in an editor.
 *
 */
public class TestResourceVisitor implements IResourceVisitor {

    private static final String BIN_FOLDER_NAME = "bin"; //$NON-NLS-1$

    private String className;
    private String methodName;
    private Collection<String> fileExtensions;
    private EditorOpener editorOpener;

    public TestResourceVisitor(String qualifiedClassName, String methodName, Collection<String> fileExtensions) {
        this.className = qualifiedClassName;
        this.methodName = methodName;
        this.fileExtensions = fileExtensions;

        this.editorOpener = new EditorOpener();
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
                Display display = PlatformUI.getWorkbench().getDisplay();
                display.asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        @SuppressWarnings({ "cast", "RedundantCast" })
                        IFile file = (IFile) resource.getAdapter(IFile.class);
                        if (file != null) {
                            try {
                                IRegion targetRegion = getClassOrMethodRegion(file);
                                if (targetRegion != null) {
                                    TestResourceVisitor.this.editorOpener.openAndSelect(activePage, file, targetRegion.getOffset(), targetRegion.getLength(), true, true);
                                }
                            } catch (PartInitException e) {
                                UiPlugin.logger().error(e.getMessage(), e);
                            } catch (BadLocationException e) {
                                UiPlugin.logger().error(e.getMessage(), e);
                            } catch (CoreException e) {
                                UiPlugin.logger().error(e.getMessage(), e);
                            }
                        }
                    }

                    private IRegion getClassOrMethodRegion(IFile file) throws BadLocationException, CoreException {
                        TextFileDocumentProvider textFileDocumentProvider = new TextFileDocumentProvider();
                        textFileDocumentProvider.connect(file);
                        IDocument document = textFileDocumentProvider.getDocument(file);
                        FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);

                        IRegion targetRegion;
                        if (TestResourceVisitor.this.methodName != null) {
                            targetRegion = findReplaceDocumentAdapter.find(0, TestResourceVisitor.this.methodName, true, true, false, false);
                            // sometimes the method name is generated and therefore cannot be found,
                            // so we open the class here
                            if (null == targetRegion) {
                                targetRegion = findReplaceDocumentAdapter.find(0, Files.getNameWithoutExtension(resource.getName()), true, true, false, false);
                            }
                        } else {
                            // if there is no methodName, we simply open the class
                            targetRegion = findReplaceDocumentAdapter.find(0, Files.getNameWithoutExtension(resource.getName()), true, true, false, false);
                        }

                        return targetRegion;
                    }
                });
                return false;
            }
        }
        return true;
    }

}
