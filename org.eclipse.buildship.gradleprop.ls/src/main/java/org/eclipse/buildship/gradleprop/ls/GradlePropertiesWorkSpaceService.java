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

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a workspace service.
 *
 * @author Nikolai Vladimirov
 */
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
