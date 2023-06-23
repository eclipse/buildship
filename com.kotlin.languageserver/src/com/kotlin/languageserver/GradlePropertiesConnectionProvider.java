package com.kotlin.languageserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
//import org.osgi.framework.FrameworkUtil;

public class GradlePropertiesConnectionProvider extends ProcessStreamConnectionProvider
		implements StreamConnectionProvider {
	public GradlePropertiesConnectionProvider() {
		System.out.println("starting...");
		try {
			Process p = new ProcessBuilder("bash", "/Users/kolavladimirov/initialize.sh").start();
			int exitCode = p.waitFor();
			if (exitCode != 0) {
				System.exit(exitCode);
			}
			System.out.println("initializing of server is success");
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}


		List<String> commands = new ArrayList<>();
		commands.add("bash");
		commands.add("/Users/kolavladimirov/launchServer.sh");
		// add in commands path to bin application of language server
		setCommands(commands);

		setWorkingDirectory(System.getProperty("user.dir"));
	}

	// new version
}
