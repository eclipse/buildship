/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * TextSourceViewerConfiguration for the {@link GradleEditor}.
 *
 */
public class GradleSourceViewerConfiguration extends TextSourceViewerConfiguration {

    private ContentAssistant contentAssistant;

    public GradleSourceViewerConfiguration(Shell shell) {
        this(null, shell);
    }

    public GradleSourceViewerConfiguration(IPreferenceStore preferenceStore, Shell shell) {
        super(preferenceStore);

        // Initialize ContentAssistant
        contentAssistant = new ContentAssistant();

        // define a default ContentAssistProcessor
        contentAssistant.setContentAssistProcessor(new GradleCompletionProcessor(shell), IDocument.DEFAULT_CONTENT_TYPE);

        // enable auto activation
        contentAssistant.enableAutoActivation(true);

        // set a proper orientation for the content assist proposal
        contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
    }

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {

        contentAssistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

        return contentAssistant;
    }

}
