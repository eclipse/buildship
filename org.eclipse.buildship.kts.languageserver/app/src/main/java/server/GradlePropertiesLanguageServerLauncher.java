package server;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import java.io.InputStream;
import java.io.OutputStream;
import org.eclipse.lsp4j.services.LanguageClient;

public class GradlePropertiesLanguageServerLauncher {

  public static void main(String[] args) throws InterruptedException, ExecutionException {
    System.err.println("server is running");
    startServer(System.in, System.out);
  }

  public static void startServer(InputStream in, OutputStream out)
      throws InterruptedException, ExecutionException {
    GradlePropertiesLanguageServer server = new GradlePropertiesLanguageServer();
    Launcher<LanguageClient> launcher = Launcher.createLauncher(server, LanguageClient.class, in,
        out);
    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);
    Future<?> startListening = launcher.startListening();
    startListening.get();
  }
}
