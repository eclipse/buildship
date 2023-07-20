/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.ls.fileSync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maintains the current file version.
 *
 * @author Nikolai Vladimirov
 */

public class FileSync {

  private static Logger LOGGER = LoggerFactory.getLogger(FileSync.class);

  final private Map<String, ContentInFile> contentByUri;

  public FileSync() {
    contentByUri = new HashMap<>();
  }

  public ContentInFile getContentByUri(String uri) {
    return contentByUri.get(uri);
  }

  public boolean openFile(String uri) {
    Path path = getPathFromUri(uri);
    try {
      String content = Files.readString(path);
      contentByUri.put(uri, new ContentInFile(content, 0));
    } catch (IOException e) {
      LOGGER.error("File with uri:" + uri + " isn't found");
      return false;
    }
    return true;
  }

  public void editFile(String uri, int version, List<TextDocumentContentChangeEvent> changes)
      throws IOException {
    ContentInFile content = contentByUri.get(uri);
    if (content == null) {
      if (!openFile(uri)) {
        LOGGER.error("File with uri:" + uri + " isn't found");
        return;
      }
    }
    assert content != null;
    String textInFile = content.getContent();
    int existingVersion = content.getVersion();

    if (existingVersion > version) {
      LOGGER.error("existing version is more than version from edit request");
      return;
    }

    for (TextDocumentContentChangeEvent change : changes) {
      String newContent = change.getText();
      if (change.getRange() != null) {
        newContent = applyChange(textInFile, change);
      }
      content.updateFile(newContent, version);
    }
  }

  public void saveFile(String uri) {
    closeFile(uri);
    openFile(uri);
  }

  public void closeFile(String uri) {
    contentByUri.remove(uri);
  }


  private Path getPathFromUri(String uri) {
    String trimmedUri = uri.substring("file://".length());
    return Paths.get(trimmedUri);
  }

  private String applyChange(String content, TextDocumentContentChangeEvent change)
      throws IOException {
    Range range = change.getRange();
    BufferedReader reader = new BufferedReader(new StringReader(content));
    StringWriter writer = new StringWriter();

    int curLine = 0;
    // skip unchanged part
    while (curLine < range.getStart().getLine()) {
      writer.write(reader.readLine() + '\n');
    }

    // write replacement text
    writer.write(change.getText());

    // skip replaced text
    int cntReplacedLines = range.getEnd().getLine() - range.getStart().getLine();
    for (int i = 0; i < cntReplacedLines; i++) {
      reader.readLine();
    }
    if (range.getStart().getLine() == range.getStart().getLine()) {
      reader.skip(range.getEnd().getCharacter() - range.getStart().getCharacter());
    } else {
      reader.skip(range.getEnd().getCharacter());
    }

    // write remaining text
    while (true) {
      int next = reader.read();
      if (next == -1) {
        return writer.toString();
      } else {
        writer.write(next);
      }
    }
  }
}
