/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.kotlindsl.provider;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.environments.IExecutionEnvironment;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Bundle;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Is a entry point to launching of a kotlin language server.
 *
 * @author Nikolai Vladimirov
 */

public class KotlinDSLConnectionProvider extends ProcessStreamConnectionProvider
    implements StreamConnectionProvider {

  public KotlinDSLConnectionProvider() {
    URL localFileURL;
    Bundle bundle = FrameworkUtil.getBundle(KotlinDSLConnectionProvider.class);
    try {
      localFileURL = FileLocator.toFileURL(bundle.getEntry("/"));
      URI localFileURI = new URI(localFileURL.toExternalForm());
      Path pathToPlugin = Paths.get(localFileURI.getPath());

      String pathToServer = pathToPlugin.resolve("libs/kotlin-language-server.jar").toString();

      IExecutionEnvironment[] executionEnvironments = JavaRuntime.getExecutionEnvironmentsManager()
          .getExecutionEnvironments();

      IExecutionEnvironment java11Environment = null;

      for (IExecutionEnvironment environment : executionEnvironments) {
        if (environment.getId().equals("JavaSE-11")) {
          java11Environment = environment;
          break;
        }
      }

      ArrayList<IVMInstall> compatibleJVMs = new ArrayList<>(
          Arrays.asList(java11Environment.getCompatibleVMs()));
      IVMInstall defaultVMInstall = JavaRuntime.getDefaultVMInstall();
      IVMInstall javaExecutable = null;

      if (!compatibleJVMs.isEmpty()) {
        if (compatibleJVMs.contains(defaultVMInstall)) {
          javaExecutable = defaultVMInstall;
        } else {
          javaExecutable = compatibleJVMs.get(0);
        }

        String pathToJavaExecutable = javaExecutable.getInstallLocation().toPath().resolve("bin")
            .resolve("java")
            .toString();

        List<String> commands = new ArrayList<>(
            Arrays.asList(pathToJavaExecutable, "-jar", pathToServer));

        // add in commands path to bin application of language server
        setCommands(commands);
        setWorkingDirectory(pathToPlugin.toString());

      } else {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
          public void run() {
            Shell shell = new Shell();
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
            messageBox.setText("Error");
            messageBox.setMessage(
                "Compatible version of Java isn't found! Install and rerun application.");
            messageBox.open();
            shell.dispose();
          }
        });
      }


    } catch (IOException | URISyntaxException e) {
      System.err.println("[GradlePropertiesConnectionProvider]:" + e.toString());
      e.printStackTrace();
    }
  }

}