/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.ls;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

/**
 * Implements a language server.
 *
 * @author Nikolai Vladimirov
 */

public class GradlePropertiesLanguageServer implements LanguageServer, LanguageClientAware {

  private final TextDocumentService textDocumentService;
  private final GradlePropertiesTextDocumentService gradleTextDocumentService;
  private final WorkspaceService workspaceService;
  private LanguageClient languageClient;
  private int shutdown = 1;

  public GradlePropertiesLanguageServer() {
    gradleTextDocumentService = new GradlePropertiesTextDocumentService();
    this.textDocumentService = gradleTextDocumentService;
    this.workspaceService = new GradlePropertiesWorkSpaceService();
  }

  @Override
  public void connect(LanguageClient client) {
    languageClient = client;
    gradleTextDocumentService.connect(client);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

    List<String> triggerCharacters = new ArrayList<String>();
    triggerCharacters.add(".");
    triggerCharacters.add("=");

    CompletionOptions options = new CompletionOptions(false, triggerCharacters);
    capabilities.setCompletionProvider(options);

    return CompletableFuture.supplyAsync(() -> new InitializeResult(capabilities));
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    shutdown = 0;
    return CompletableFuture.supplyAsync(Object::new);
  }

  @Override
  public void exit() {
    System.exit(shutdown);
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return textDocumentService;
  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return workspaceService;
  }

}
