/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.editor;

import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * @author Christophe Moine
 *
 */
public class GradleTextViewerConfiguration extends TextSourceViewerConfiguration {
    /**
     *
     * @param editor the editor we're creating.
     * @param preferenceStore the preference store.
     */
    public GradleTextViewerConfiguration() {
        super(EditorsUI.getPreferenceStore());
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        GradlePresentationReconciler reconciler = new GradlePresentationReconciler();
        reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
        return reconciler;
    }

    @Override
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        return GradleEditorConstants.PARTITIONS;
    }

    @Override
    public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
        return GradleEditorConstants.PARTITIONING;
    }
}
