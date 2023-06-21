package server.diagnostic;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import server.completion.PropertiesStorage;
import server.fileSync.ContentInFile;

public class DiagnosticManager {

  static public Diagnostic getOnlyValueError(String line, int strNumber) {
    var occasionOfEqual = line.indexOf("=");
    if (occasionOfEqual == -1) {
      return null;
    }

    return checkValueError(line, strNumber, occasionOfEqual);
  }

  static public List<Diagnostic> getDiagnosticList(ContentInFile content) {
    List<Diagnostic> errors = new ArrayList<>();

    String text = content.getContent();
    var lines = text.split("\n");
    for (int i = 0; i < lines.length; i++) {
      var line = lines[i];
      Diagnostic error = getOnlyValueError(line, i);
      // validateLine is maybe useful
      if (error != null) {
        errors.add(error);
      }
    }
    for (var err : errors) {
      System.err.println(err.toString());
    }
    return errors;

  }

  static private Diagnostic validateLine(String line, int strNumber) {
    int occasionOfComment = line.indexOf('#');
    if (occasionOfComment != -1) {
      line = line.substring(0, occasionOfComment);
    }
    if (line.isEmpty()) {
      return null;
    }

    var words = line.split("\\s+");
    if (words.length > 1) {
      return new Diagnostic(
          new Range(new Position(strNumber, 0), new Position(strNumber, line.length())),
          "format of each line in gradle.properties is PROPERTY=VALUE without space symbols");
    }
    words = line.split("=");
    if (words.length > 2) {
      return new Diagnostic(
          new Range(new Position(strNumber, 0), new Position(strNumber, line.length())),
          "format of each line in gradle.properties is PROPERTY=VALUE without space symbols");
    }

    var occasionOfEqual = line.indexOf("=");
    if (occasionOfEqual == -1) {
      return null;
    }

    return checkValueError(line, strNumber, occasionOfEqual);
  }

  static private Diagnostic checkValueError(String line, int strNumber, int occasionOfEqual) {
    var potentialProperty = line.substring(0, occasionOfEqual);
    var value = line.substring(occasionOfEqual + 1).trim();
    if (value.isEmpty()) {
      return new Diagnostic(new Range(new Position(strNumber, occasionOfEqual + 1),
          new Position(strNumber, line.length())),
          "enter value after property name");
    }

    var allProperties = PropertiesStorage.getProperties();
    for (server.completion.Property curProperty : allProperties) {
      if (potentialProperty.equals(curProperty.getName())) {
        if (curProperty.getValues().isEmpty()) {
          return null;
        }
        for (var correctValue : curProperty.getValues()) {
          if (correctValue.equals(value)) {
            return null;
          }
        }
        return new Diagnostic(
            new Range(new Position(strNumber, occasionOfEqual + 1),
                new Position(strNumber, line.length())),
            "wrong value, list of correct values:" + curProperty.getValues().toString());
      }
    }
    return null;
  }
}
