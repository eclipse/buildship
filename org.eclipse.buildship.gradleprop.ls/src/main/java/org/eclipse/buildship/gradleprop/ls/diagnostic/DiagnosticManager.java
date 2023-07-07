package org.eclipse.buildship.gradleprop.ls.diagnostic;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.buildship.gradleprop.ls.completion.Property;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.buildship.gradleprop.ls.completion.PropertiesStorage;
import org.eclipse.buildship.gradleprop.ls.fileSync.ContentInFile;

public class DiagnosticManager {

  static public List<Diagnostic> getDiagnosticList(ContentInFile content) {
    List<Diagnostic> errors = new ArrayList<>();
    String text = content.getContent();
    String[] lines = text.split("\n");
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      Diagnostic error = checkValueError(line, i);
      if (error != null) {
        errors.add(error);
      }
    }
    return errors;

  }

  static private Diagnostic checkValueError(String line, int strNumber) {
    int occasionOfEqual = line.indexOf("=");
    if (occasionOfEqual == -1) {
      return null;
    }

    String potentialProperty = line.substring(0, occasionOfEqual).trim();
    String value = line.substring(occasionOfEqual + 1).trim();

    List<Property> allProperties = PropertiesStorage.getProperties();
    for (org.eclipse.buildship.gradleprop.ls.completion.Property curProperty : allProperties) {
      if (potentialProperty.equals(curProperty.getName())) {
        if (curProperty.getValues().isEmpty()) {
          return null;
        }
        for (String correctValue : curProperty.getValues()) {
          if (correctValue.equals(value)) {
            return null;
          }
        }
        return new Diagnostic(
            new Range(new Position(strNumber, 0),
                new Position(strNumber, line.length() - 1)),
            "invalid value, use one of the following items:" + curProperty.getValues().toString());
      }
    }
    return null;
  }
}
