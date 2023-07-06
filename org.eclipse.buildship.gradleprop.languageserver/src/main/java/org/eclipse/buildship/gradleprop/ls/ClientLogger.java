package org.eclipse.buildship.gradleprop.ls;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * This class is used for logging and will be removed later.
 */
public class ClientLogger {

  private static ClientLogger INSTANCE;
  private LanguageClient client;
  private boolean isInitialized;

  private ClientLogger() {
  }

  public void initialize(LanguageClient languageClient) {
    if (!Boolean.TRUE.equals(isInitialized)) {
      this.client = languageClient;
    }
    isInitialized = true;
  }

  public static ClientLogger getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ClientLogger();
    }
    return INSTANCE;
  }

  public void logMessage(String message) {
    if (!isInitialized) {
      return;
    }
    client.logMessage(new MessageParams(MessageType.Info, message));
  }
}
