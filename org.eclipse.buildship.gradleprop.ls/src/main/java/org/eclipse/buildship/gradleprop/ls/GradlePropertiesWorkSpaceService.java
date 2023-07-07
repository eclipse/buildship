package org.eclipse.buildship.gradleprop.ls;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GradlePropertiesWorkSpaceService implements WorkspaceService {

  private static Logger LOGGER = LoggerFactory.getLogger(GradlePropertiesWorkSpaceService.class);


  public GradlePropertiesWorkSpaceService() {

  }

  @Override
  public void didChangeConfiguration(DidChangeConfigurationParams params) {
    LOGGER.info("Operation 'workspace/didChangeConfiguration'");
  }

  @Override
  public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
    LOGGER.info("Operation 'workspace/didChangeWatchedFiles'");
  }
}
