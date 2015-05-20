package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextEditor;


public class GradleEditor extends TextEditor {

    public static final String ID = "org.eclipse.buildship.ui.gradlebuildscripteditor"; //$NON-NLS-1$

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        Shell shell = getSite().getShell();
        setSourceViewerConfiguration(new GradleSourceViewerConfiguration(shell));

        return super.createSourceViewer(parent, ruler, styles);
    }
}
