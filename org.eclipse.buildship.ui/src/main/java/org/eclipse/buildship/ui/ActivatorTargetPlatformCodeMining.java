package org.eclipse.buildship.ui;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
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

import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;

/**
 * Project reference mining.
 */
@SuppressWarnings("restriction")
public class ActivatorTargetPlatformCodeMining extends LineHeaderCodeMining {

	private IPath path;
	private Boolean isValid;

	public ActivatorTargetPlatformCodeMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider,
			Boolean isValid)
			throws BadLocationException {
		super(beforeLineNumber, document, provider);

		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		this.path = bufferManager.getTextFileBuffer(document).getLocation();
		this.isValid = isValid;
	}

	@Override
	protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
		return CompletableFuture.runAsync(() -> {
//			if(!this.isValid) {
				super.setLabel("Sync Gradle build");
				return;
//			}
		});
	}

	@Override
	public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
	    Image image = getImage();
        gc.drawImage(image, x, y + gc.getFontMetrics().getDescent());
        Rectangle bounds = image.getBounds();
        gc.drawText("Sync Gradle build", x + bounds.width, y);
        return new Point(bounds.width, bounds.height);
	}

	private Image getImage() {
	    return PluginImages.REFRESH.withState(ImageState.DISABLED).getImage();
    }

    @Override
	public Consumer<MouseEvent> getAction() {
		return t -> activateTargetPlatform();
	}

	private void activateTargetPlatform() {
	    System.out.println("TODO: exec gradle sync");
	}
}
