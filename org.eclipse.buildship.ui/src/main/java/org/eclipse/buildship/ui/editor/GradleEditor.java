/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import org.eclipse.ui.editors.text.TextEditor;

/**
 * Gradle Editor entry point
 *
 * @author Christophe Moine
 */
public class GradleEditor extends TextEditor {
    @Override
    protected void initializeEditor() {
        setSourceViewerConfiguration(new GradleTextViewerConfiguration());
        super.initializeEditor();
    }
}
