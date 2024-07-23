/*******************************************************************************
 * Copyright (c) 2024 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.compare;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.io.CharStreams;

import org.eclipse.compare.IEncodedStreamContentAccessor;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.buildship.ui.internal.UiPlugin;
import org.eclipse.buildship.ui.internal.editor.GradleDocumentSetupParticipant;
import org.eclipse.buildship.ui.internal.editor.GradleTextViewerConfiguration;

public final class GradleViewer extends Viewer {

    private final SourceViewer sourceViewer;

    private Object input;

    public GradleViewer(Composite parent) {
        this.sourceViewer = new SourceViewer(parent, null, SWT.H_SCROLL | SWT.V_SCROLL);
        this.sourceViewer.setEditable(false);
        this.sourceViewer.configure(new GradleTextViewerConfiguration());

        // use the same font as the TextMergeViewer
        this.sourceViewer.getTextWidget().setFont(JFaceResources.getFont(TextMergeViewer.class.getName()));
    }

    @Override
    public Control getControl() {
        return this.sourceViewer.getControl();
    }

    @Override
    public Object getInput() {
        return this.input;
    }

    @Override
    public ISelection getSelection() {
        return StructuredSelection.EMPTY;
    }

    @Override
    public void refresh() {
        // empty implementation
    }

    @Override
    public void setInput(Object input) {
        this.input = input;

        if (!(input instanceof IStreamContentAccessor && input instanceof IEncodedStreamContentAccessor)) {
            return;
        }

        IEncodedStreamContentAccessor contentAccessor = (IEncodedStreamContentAccessor) input;
        try (InputStream contents = contentAccessor.getContents()) {
            if (contents == null) {
                return;
            }

            String charset = contentAccessor.getCharset();
            if (charset == null) {
                charset = ResourcesPlugin.getEncoding();
            }

            InputStreamReader reader = new InputStreamReader(contents, charset);
            Document document = new Document(CharStreams.toString(reader));
            new GradleDocumentSetupParticipant().setup(document);
            this.sourceViewer.setDocument(document);
        } catch (CoreException | IOException e) {
            UiPlugin.logger().error("Failed to set up input document.", e);
        }
    }

    @Override
    public void setSelection(ISelection selection, boolean reveal) {
        // empty implementation
    }

}
