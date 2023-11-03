/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.ls;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Launches the language server. Is invoked by the org.eclipse.buildship.gradleprop.provider.
 *
 * @author Nikolai Vladimirov
 */
public class GradlePropertiesLanguageServerLauncher {

  private static final Logger LOGGER = LoggerFactory.getLogger(GradlePropertiesLanguageServerLauncher.class);

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    LOGGER.info("Gradle properties language server started");
    startServer(System.in, System.out);
  }

  public static void startServer(InputStream in, OutputStream out)
      throws InterruptedException, ExecutionException {
    GradlePropertiesLanguageServer server = new GradlePropertiesLanguageServer();
    Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, in, out);
    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);
    Future<?> startListening = launcher.startListening();
    startListening.get();
  }
}
