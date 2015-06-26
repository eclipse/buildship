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

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Editor for Gradle build files.
 *
 */
public class GradleEditor extends TextEditor {

    public static final String ID = "org.eclipse.buildship.ui.gradlebuildscripteditor"; //$NON-NLS-1$

    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        Shell shell = getSite().getShell();
        setSourceViewerConfiguration(new GradleSourceViewerConfiguration(shell));

        return super.createSourceViewer(parent, ruler, styles);
    }
}
