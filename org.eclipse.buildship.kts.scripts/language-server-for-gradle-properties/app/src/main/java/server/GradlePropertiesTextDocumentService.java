package server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentDiagnosticParams;
import org.eclipse.lsp4j.DocumentDiagnosticReport;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightKind;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.adapters.DocumentDiagnosticReportTypeAdapter;
import org.eclipse.lsp4j.jsonrpc.json.ResponseJsonAdapter;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import server.completion.PropertiesMatcher;
import server.diagnostic.DiagnosticManager;
import server.fileSync.FileSync;

public class GradlePropertiesTextDocumentService implements TextDocumentService {

  final private ClientLogger clientLogger;
  private final FileSync sources;

  private LanguageClient languageClient;

  private void publishDiagnostic(String uri) {
    if (languageClient != null) {
      var diagnosticList = DiagnosticManager.getDiagnosticList(sources.getContentByUri(uri));

      languageClient.publishDiagnostics(new PublishDiagnosticsParams(uri, diagnosticList));
    }
  }

  public GradlePropertiesTextDocumentService() {
    clientLogger = ClientLogger.getInstance();
    sources = new FileSync();
  }

  public void connect(LanguageClient client) {
    languageClient = client;
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    clientLogger.logMessage("Operation '" + "text/didOpen");
    var uri = params.getTextDocument().getUri();
    sources.openFile(uri);

    publishDiagnostic(uri);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    clientLogger.logMessage("Operation '" + "text/didChange");
    var uri = params.getTextDocument().getUri();
    var changes = params.getContentChanges();
    var version = params.getTextDocument().getVersion();

    try {
      sources.editFile(uri, version, changes);
    } catch (IOException e) {
      System.err.println("did change exception");
    }
    publishDiagnostic(uri);
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    clientLogger.logMessage("Operation '" + "text/didClose");
    var uri = params.getTextDocument().getUri();
    sources.closeFile(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    clientLogger.logMessage("Operation '" + "text/didSave");
    var uri = params.getTextDocument().getUri();
    sources.saveFile(uri);
    publishDiagnostic(uri);
  }

  @JsonRequest
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      CompletionParams position) {
    var uri = position.getTextDocument().getUri();
    var contentInFile = sources.getContentByUri(uri);

    // first update always is lost -> on first update program doesn't know about new symbol
    // and last word is taken wrong
    if (contentInFile.getVersion() < 2) {
      return CompletableFuture.supplyAsync(() -> Either.forLeft(new ArrayList<>()));
    }
    // match with properties and make the list of completions
    var completions = PropertiesMatcher.getCompletions(contentInFile.getContent(),
        position.getPosition());
    return CompletableFuture.supplyAsync(() -> Either.forLeft(completions));
  }
}
