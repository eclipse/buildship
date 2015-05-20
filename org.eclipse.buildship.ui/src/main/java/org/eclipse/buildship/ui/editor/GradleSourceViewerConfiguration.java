package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

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
