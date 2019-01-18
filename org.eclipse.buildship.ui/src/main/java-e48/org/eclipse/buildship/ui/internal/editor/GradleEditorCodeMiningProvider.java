/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.editor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;

/**
 * Contributes Gradle project synchronization to code mining.
 *
 * @author Donat Csikos
 */
public class GradleEditorCodeMiningProvider extends AbstractCodeMiningProvider {

    @Override
    public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Arrays.asList(new ProjectSynchronizerCodeMining(viewer.getDocument(), this));
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
    }
}