/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.editor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.internal.workspace.NewProjectHandler;
import org.eclipse.buildship.core.internal.workspace.SynchronizationJob;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * Adds synchronize action code mining.
 *
 * @author Donat Csikos
 */
public class ProjectSynchronizerCodeMining extends LineHeaderCodeMining {

    private static final String REFRESH_LABEL = "Refresh Gradle build";

    private final IDocument document;

    public ProjectSynchronizerCodeMining(IDocument document, ICodeMiningProvider provider) throws BadLocationException {
        super(0, document, provider);
        this.document = document;
    }

    @Override
    protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
        return CompletableFuture.runAsync(() -> {
            super.setLabel(REFRESH_LABEL);
            return;
        });
    }

    @Override
    public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
        int imageSize = textWidget.getLineHeight(); // rectangular image
        Image image = getImage();
        Rectangle imageBounds = image.getBounds();
        gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, x, y, imageSize, imageSize);
        gc.drawText(REFRESH_LABEL, x + imageSize, y);
        return new Point(imageSize + gc.textExtent(REFRESH_LABEL).x, imageSize);
    }

    private Image getImage() {
        return PluginImages.REFRESH.withState(ImageState.DISABLED).getImage();
    }

    @Override
    public Consumer<MouseEvent> getAction() {
        return t -> executeSynchronization(t);
    }

    private void executeSynchronization(MouseEvent t) {
        if (this.document != null) {
            ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
            if (manager != null) {
                ITextFileBuffer buffer = manager.getTextFileBuffer(this.document);
                if (buffer != null) {
                    IPath path = buffer.getLocation();
                    if (path != null) {
                        IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
                        if (resource != null) {
                            IProject project = resource.getProject();
                            GradleCore.getWorkspace().getBuild(project).ifPresent(build -> new SynchronizationJob(NewProjectHandler.IMPORT_AND_MERGE, build).schedule());
                        }
                    }
                }
            }
        }
    }
}
