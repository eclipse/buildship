package com.kotlin.languageserver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;

public class GradlePropertiesConnectionProvider extends ProcessStreamConnectionProvider implements StreamConnectionProvider {
	public GradlePropertiesConnectionProvider() {
		System.out.println("starting server...");
		
		List<String> commands = new ArrayList<>();
		
		// add in commands path to bin application of language server
		
		setCommands(commands);
		
		setWorkingDirectory(System.getProperty("user.dir"));
	}
	
}
