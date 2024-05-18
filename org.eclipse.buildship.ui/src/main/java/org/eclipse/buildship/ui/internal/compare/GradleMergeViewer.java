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

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.buildship.ui.internal.editor.GradleEditorConstants;
import org.eclipse.buildship.ui.internal.editor.GradlePartitionScanner;
import org.eclipse.buildship.ui.internal.editor.GradleTextViewerConfiguration;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;

public final class GradleMergeViewer extends TextMergeViewer {

    public GradleMergeViewer(Composite parent, CompareConfiguration configuration) {
        super(parent, configuration);
    }

    @Override
    public String getTitle() {
        return UiMessages.Title_Gradle_Build_Script_Compare;
    }

    @Override
    protected void configureTextViewer(TextViewer textViewer) {
        if (textViewer instanceof ISourceViewer) {
            ((ISourceViewer) textViewer).configure(new GradleTextViewerConfiguration());
        }
    }

    @Override
    protected IDocumentPartitioner getDocumentPartitioner() {
        return new FastPartitioner(new GradlePartitionScanner(), GradleEditorConstants.PARTITIONS);
    }

    @Override
    protected String getDocumentPartitioning() {
        return GradleEditorConstants.PARTITIONING;
    }

}
