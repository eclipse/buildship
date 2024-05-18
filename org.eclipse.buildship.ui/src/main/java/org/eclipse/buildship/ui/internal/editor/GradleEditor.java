/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.editor;

import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.ForwardingDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * Editor definition for Gradle build scripts.
 *
 * @author Christophe Moine
 */
public final class GradleEditor extends TextEditor {

    private static final IDocumentProvider DOCUMENT_PROVIDER = new ForwardingDocumentProvider(GradleEditorConstants.PARTITIONING, new GradleDocumentSetupParticipant(),
            new TextFileDocumentProvider());

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setSourceViewerConfiguration(new GradleTextViewerConfiguration());

        // This ensures that the document is set up correctly when it is opened from the History
        // View.
        setDocumentProvider(DOCUMENT_PROVIDER);
    }

    @Override
    protected boolean isTabsToSpacesConversionEnabled() {
        // Can't use our own preference store because JDT disables this functionality in its preferences.
        return EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
    }
}
