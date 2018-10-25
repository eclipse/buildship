package org.eclipse.buildship.ui;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;

public class SampleCodeMiningProvider extends AbstractCodeMiningProvider {

	@Override
	public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
			IProgressMonitor monitor) {
		return CompletableFuture.supplyAsync(() -> {
            try {
                return Arrays.asList(new ActivatorTargetPlatformCodeMining(0, viewer.getDocument(), this, true));
            } catch (BadLocationException e) {
                throw new RuntimeException();
            }
		});
	}
}