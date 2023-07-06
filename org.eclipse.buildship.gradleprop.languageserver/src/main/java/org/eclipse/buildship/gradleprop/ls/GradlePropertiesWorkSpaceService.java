package org.eclipse.buildship.gradleprop.ls;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;

public class GradlePropertiesWorkSpaceService implements WorkspaceService {

  private final ClientLogger clientLogger;

  public GradlePropertiesWorkSpaceService() {
    clientLogger = ClientLogger.getInstance();
  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    clientLogger.logMessage("Operation 'workspace/didChangeConfiguration' Ack");
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    clientLogger.logMessage("Operation 'workspace/didChangeWatchedFiles' Ack");
  }
}