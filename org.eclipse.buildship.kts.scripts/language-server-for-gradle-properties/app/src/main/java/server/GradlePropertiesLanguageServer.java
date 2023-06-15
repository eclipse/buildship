package server;

import java.util.ArrayList;
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

public class GradlePropertiesLanguageServer implements LanguageServer, LanguageClientAware {

  private TextDocumentService textDocumentService;
  private WorkspaceService workspaceService;
  //    private ClientCapabilities clientCapabilities;
  LanguageClient languageClient;
  private int shutdown = 1;

  public GradlePropertiesLanguageServer() {
    this.textDocumentService = new GradlePropertiesTextDocumentService();
    this.workspaceService = new GradlePropertiesWorkSpaceService();
  }

  @Override
  public void connect(LanguageClient client) {
    languageClient = client;
    ClientLogger.getInstance().initialize(languageClient);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
    System.err.println("[LS]: initializing");
    var capabilities = new ServerCapabilities();
    //Set the document synchronization capabilities to full.
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);

    var triggerCharacters = new ArrayList<String>();
    triggerCharacters.add(".");

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
