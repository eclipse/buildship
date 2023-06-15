package server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.TextDocumentService;
import server.completeGradleProperties.GradleProperties;
import server.completeGradleProperties.PropertiesMatcher;
import server.file.SourceManager;

public class GradlePropertiesTextDocumentService implements TextDocumentService {

  final private ClientLogger clientLogger;
  final private SourceManager sources;

  public GradlePropertiesTextDocumentService() {

    this.clientLogger = ClientLogger.getInstance();
    sources = new SourceManager();
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    this.clientLogger.logMessage("Operation '" + "text/didOpen");
    var uri = params.getTextDocument().getUri();
    sources.openFile(uri);
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    this.clientLogger.logMessage("Operation '" + "text/didChange");
    var uri = params.getTextDocument().getUri();
    var changes = params.getContentChanges();
    var version = params.getTextDocument().getVersion();
    try {
      sources.editFile(uri, version, changes);
    } catch (IOException e) {
      System.err.println("did change exception");
    }

  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    this.clientLogger.logMessage("Operation '" + "text/didClose");
    var uri = params.getTextDocument().getUri();
    sources.closeFile(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    this.clientLogger.logMessage("Operation '" + "text/didSave");
    var uri = params.getTextDocument().getUri();
    sources.saveFile(uri);
  }

  @JsonRequest
  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
      CompletionParams position) {

    var uri = position.getTextDocument().getUri();
    var content = sources.getContentByUri(uri).getContent();

    // DEBUG ONLY
    var lines_arr = content.split("\n");
    System.err.println("========text=======");
    for (var l : lines_arr) {
      System.err.println(l);
    }
    Position positionOfCursor = position.getPosition();
    System.err.println("========text=======");
    System.err.println("[completion] character:" + positionOfCursor.getCharacter() + " line:"
        + positionOfCursor.getLine());
    // DEBUG ONLY

    // match with properties and make the list of completions
    var completions = PropertiesMatcher.getCompletions(content, position.getPosition());
    return CompletableFuture.supplyAsync(() -> Either.forLeft(completions));
  }

}
