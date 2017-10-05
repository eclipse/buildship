/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

/**
 * The document setup participant for a Gradle build script document.
 *
 * @author Christophe Moine
 */
public final class GradleDocumentSetupParticipant implements IDocumentSetupParticipant {

    @Override
    public void setup(IDocument document) {
        IDocumentPartitioner partitioner = createDocumentPartitioner();
        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension3 = (IDocumentExtension3) document;
            extension3.setDocumentPartitioner(GradleEditorConstants.PARTITIONING, partitioner);
        } else {
            document.setDocumentPartitioner(partitioner);
        }
        partitioner.connect(document);
    }

    private static IDocumentPartitioner createDocumentPartitioner() {
        return new FastPartitioner(new GradlePartitionScanner(), GradleEditorConstants.PARTITIONS);
    }
}