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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.buildship.gradleprop.ls.fileSync.ContentInFile;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.buildship.gradleprop.ls.completion.PropertiesMatcher;
import org.eclipse.buildship.gradleprop.ls.diagnostic.DiagnosticManager;
import org.eclipse.buildship.gradleprop.ls.fileSync.FileSync;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a text document service.
 *
 * @author Nikolai Vladimirov
 */

public class GradlePropertiesTextDocumentService implements TextDocumentService {

  private static Logger LOGGER = LoggerFactory.getLogger(GradlePropertiesTextDocumentService.class);
  private final FileSync sources;

  private LanguageClient languageClient;

  private void publishDiagnostic(String uri) {
    ContentInFile content = sources.getContentByUri(uri);
    if (languageClient != null && content != null) {
      List<Diagnostic> diagnosticList = DiagnosticManager.getDiagnosticList(content);

      languageClient.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnosticList));
    }
  }

  public GradlePropertiesTextDocumentService() {
    sources = new FileSync();
  }

  public void connect(LanguageClient client) {
    languageClient = client;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    LOGGER.info("operation /didOpen");
    String uri = params.getTextDocument().getUri();

    if (!sources.openFile(uri)) {
      return;
    }

    publishDiagnostic(uri);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    LOGGER.info("operation /didChange");
    String uri = params.getTextDocument().getUri();
    List<TextDocumentContentChangeEvent> changes = params.getContentChanges();
    Integer version = params.getTextDocument().getVersion();

    try {
      sources.editFile(uri, version, changes);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      return;
    }
    publishDiagnostic(uri);
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    LOGGER.info("operation /didClose");
    String uri = params.getTextDocument().getUri();
    sources.closeFile(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    LOGGER.info("operation /didSave");
    String uri = params.getTextDocument().getUri();
    sources.saveFile(uri);
    publishDiagnostic(uri);
  }

  @JsonRequest
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      CompletionParams position) {
    String uri = position.getTextDocument().getUri();
    ContentInFile content = sources.getContentByUri(uri);

    // first update always is lost -> on first update program doesn't know about new symbol
    // and last word is taken wrong
    if (content.getVersion() < 2) {
      return CompletableFuture.supplyAsync(() -> Either.forLeft(new ArrayList<>()));
    }
    // match with properties and make the list of completions
    List<CompletionItem> completions = PropertiesMatcher.getCompletions(content.getContent(),
        position.getPosition());
    return CompletableFuture.supplyAsync(() -> Either.forLeft(completions));
  }
}
