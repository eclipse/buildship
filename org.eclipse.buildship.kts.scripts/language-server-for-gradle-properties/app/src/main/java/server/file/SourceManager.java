package server.file;

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
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;

public class SourceManager {

  final private Map<String, ContentInFile> contentByUri;

  public SourceManager() {
    contentByUri = new HashMap<>();
  }

  public ContentInFile getContentByUri(String uri) {
    return contentByUri.get(uri);
  }

  public void openFile(String uri) {
    Path path = getPathFromUri(uri);
    try {
      String content = Files.readString(path);
      contentByUri.put(uri, new ContentInFile(content, 0));
    } catch (IOException e) {
      System.err.println("file isn't found, uri:" + uri);
      throw new RuntimeException(e);
    }

  }

  public void editFile(String uri, int version, List<TextDocumentContentChangeEvent> changes)
      throws IOException {
    var contentInFile = contentByUri.get(uri);
    var content = contentInFile.getContent();
    var existingVersion = contentInFile.getVersion();

    if (existingVersion > version) {
      return;
    }

    for (var change : changes) {
      String newContent = change.getText();
      if (change.getRange() != null) {
        newContent = applyChange(content, change);
      }
      contentInFile.updateFile(newContent, version);
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
    var trimmedUri = uri.substring("file://".length());
    return Paths.get(trimmedUri);
  }

  private String applyChange(String content, TextDocumentContentChangeEvent change)
      throws IOException {
    var range = change.getRange();
    var reader = new BufferedReader(new StringReader(content));
    var writer = new StringWriter();

    var curLine = 0;
    // skip unchanged part
    while (curLine < range.getStart().getLine()) {
      writer.write(reader.readLine() + '\n');
    }

    // write replacement text
    writer.write(change.getText());

    // skip replaced text
    var cntReplacedLines = range.getEnd().getLine() - range.getStart().getLine();
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
      var next = reader.read();
      if (next == -1) {
        return writer.toString();
      } else {
        writer.write(next);
      }
    }
  }
}
