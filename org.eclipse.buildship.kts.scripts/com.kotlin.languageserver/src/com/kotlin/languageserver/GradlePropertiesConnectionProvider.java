package com.kotlin.languageserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class GradlePropertiesConnectionProvider extends ProcessStreamConnectionProvider implements StreamConnectionProvider {
	public GradlePropertiesConnectionProvider() {
		System.out.println("starting...");
		
		List<String> commands = new ArrayList<>();
		commands.add("bash");
		commands.add("/Users/kolavladimirov/launchServer.sh");

		// add in commands path to bin application of language server
		setCommands(commands);
		setWorkingDirectory(System.getProperty("user.dir"));
	}
	
}
