package org.eclipse.buildship.gradleprop.provider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.core.runtime.FileLocator;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Bundle;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;


public class GradlePropertiesConnectionProvider extends ProcessStreamConnectionProvider
		implements StreamConnectionProvider {
	public GradlePropertiesConnectionProvider() {
		URL localFileURL;
		Bundle bundle = FrameworkUtil.getBundle(GradlePropertiesConnectionProvider.class);
		try {
			localFileURL = FileLocator.toFileURL(bundle.getEntry("/"));
			URI localFileURI= new URI(localFileURL.toExternalForm());
			Path p = Paths.get(localFileURI.getPath());

			String pathToServer = p.resolve("libs/language-server.jar").toString();

			List<String> commands = new ArrayList<>();
			commands.add("java");
			commands.add("-jar");
			commands.add(pathToServer);
			// add in commands path to bin application of language server
			setCommands(commands);
			setWorkingDirectory(p.toString());

		} catch (IOException | URISyntaxException e) {
			System.err.println("[GradlePropertiesConnectionProvider]:" + e.toString());
			e.printStackTrace();
		}

	}

}
