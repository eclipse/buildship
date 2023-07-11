/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.provider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Bundle;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Is a entry point to launching of a language server for gradle properties.
 *
 * @author Nikolai Vladimirov
 */

public class GradlePropertiesConnectionProvider extends ProcessStreamConnectionProvider
    implements StreamConnectionProvider {

  public GradlePropertiesConnectionProvider() {
    URL localFileURL;
    Bundle bundle = FrameworkUtil.getBundle(GradlePropertiesConnectionProvider.class);
    try {
      localFileURL = FileLocator.toFileURL(bundle.getEntry("/"));
      URI localFileURI = new URI(localFileURL.toExternalForm());
      Path pathToPlugin = Paths.get(localFileURI.getPath());

      String pathToServer = pathToPlugin.resolve("libs/language-server.jar").toString();

      IVMInstall defaultVMInstall = JavaRuntime.getDefaultVMInstall();
      String javaExecutable = null;
      if (defaultVMInstall != null) {
        javaExecutable = defaultVMInstall.getInstallLocation().toPath().resolve("bin")
            .resolve("java").toString();
      } else {
        IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
        for (int i = 0, find = 0; i < types.length && find == 0; i++) {
          IVMInstallType type = types[i];
          IVMInstall[] jres = type.getVMInstalls();
          for (IVMInstall jre : jres) {
            javaExecutable = jre.getInstallLocation().toPath().resolve("bin").resolve("java")
                .toString();
            find = 1;
            break;
          }
        }
      }

      List<String> commands = new ArrayList<>();

      if (javaExecutable == null) {
        Display.getDefault().syncExec(new Runnable() {
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

      } else {
        commands.add(javaExecutable);
        commands.add("-jar");
        commands.add(pathToServer);
      }

      // add in commands path to bin application of language server
      setCommands(commands);

      setWorkingDirectory(pathToPlugin.toString());

    } catch (IOException | URISyntaxException e) {
      System.err.println("[GradlePropertiesConnectionProvider]:" + e.toString());
      e.printStackTrace();
    }
  }

}
