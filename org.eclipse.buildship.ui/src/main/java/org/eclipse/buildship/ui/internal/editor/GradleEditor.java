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
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

/**
 * Editor definition for Gradle build scripts.
 *
 * @author Christophe Moine
 */
public final class GradleEditor extends TextEditor {
    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setSourceViewerConfiguration(new GradleTextViewerConfiguration());
    }

    @Override
    protected boolean isTabsToSpacesConversionEnabled() {
        // Can't use our own preference store because JDT disables this functionality in its preferences.
        return EditorsUI.getPreferenceStore().getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS);
    }
}
