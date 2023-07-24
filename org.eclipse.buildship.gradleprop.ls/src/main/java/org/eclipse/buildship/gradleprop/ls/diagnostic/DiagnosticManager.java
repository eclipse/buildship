/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.ls.diagnostic;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.buildship.gradleprop.ls.completion.Property;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.buildship.gradleprop.ls.completion.PropertiesStorage;
import org.eclipse.buildship.gradleprop.ls.fileSync.ContentInFile;

/**
 * Is responsible for the validation of a file.
 *
 * @author Nikolai Vladimirov
 */
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
