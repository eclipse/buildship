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
		System.out.println("starting...");
		
		
		URL localFileURL;
		String launch = "";
		Bundle bundle = FrameworkUtil.getBundle(GradlePropertiesConnectionProvider.class);
		try {
			
			localFileURL = FileLocator.toFileURL(bundle.getEntry("/"));
			URI localFileURI= new URI(localFileURL.toExternalForm());
			
			Path p = Paths.get(localFileURI.getPath());

			launch = p.resolve("jars/server.jar").toString();
			System.err.println("init pathes");
			
//			System.err.println(new File(jar.toString()).exists());
//			Process pr = new ProcessBuilder("bash", init.toString()).start();
//			int exitCode = pr.waitFor();
//			if (exitCode != 0) {
//				System.err.println("exit code is " + exitCode);
//				System.exit(exitCode);
//			}
			System.out.println("initializing of server is success");
	        
	        
	        List<String> commands = new ArrayList<>();
			commands.add("java");
			commands.add("-jar");
			commands.add(launch);
			// add in commands path to bin application of language server
			setCommands(commands);

			setWorkingDirectory(p.toString());
	        
		} catch (IOException | URISyntaxException e) {
			// TODO Auto-generated catch block
			System.out.println("error is catched:" + e.toString());
			e.printStackTrace();
		}

	}

}
