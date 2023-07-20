/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.ls.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gives completion items by content and position in a file.
 *
 * @author Nikolai Vladimirov
 */
public class PropertiesMatcher {

  private static Logger LOGGER = LoggerFactory.getLogger(PropertiesMatcher.class);

  /**
   * The common function that is invoked from outside.
   *
   * @param content is the text in a file and position in the file.
   * @return list of completion items.
   */
  public static List<CompletionItem> getCompletions(String content, Position position) {
    if (!positionInsideContent(content, position)) {
      LOGGER.error("Position of cursor isn't placed inside the content");
      return new ArrayList<>();
    }

    String completionWord = getCompletionWord(content, position);
    char lastSymbol = '\0';
    if (!completionWord.isEmpty()) {
      lastSymbol = completionWord.charAt(completionWord.length() - 1);
    }
    boolean onlyValues = false;

    // if it's true, we consider only values
    if (lastSymbol == '=') {
      completionWord = completionWord.substring(0, completionWord.length() - 1);
      onlyValues = true;
    }

    if (onlyValues) {
      List<CompletionItem> result = new ArrayList<>();
      for (Property property : PropertiesStorage.getProperties()) {
        if (property.getName().equals(completionWord)) {
          for (String value : property.getValues()) {
            result.add(new CompletionItem(value));
          }
        }
      }
      return result;
    }

    List<Property> matchedProperties = getMatchedProperties(completionWord);

    return matchedProperties.stream()
        .map(property -> {
          CompletionItem item = new CompletionItem();
          item.setLabel(property.getName());
          item.setDocumentation(property.getDescription());
          return item;
        })
        .collect(Collectors.toList());
  }

  /**
   * Gives word which is placed to the left of the work cursor.
   */
  private static String getCompletionWord(String content, Position position) {
    String[] lines = content.split("\n");
    if (position.getCharacter() == 0) {
      return "";
    }
    String workLine = lines[position.getLine()].substring(0, position.getCharacter());
    String[] wordsOnLine = workLine.split("\\s+");
    if (wordsOnLine.length == 0) {
      return "";
    }
    return wordsOnLine[wordsOnLine.length - 1];
  }


  static private List<Property> getMatchedProperties(String input) {
    List<Property> result = new ArrayList<>();
    if (input.isEmpty()) {
      return result;
    }
    List<Property> properties = PropertiesStorage.getProperties();

    for (Property property : properties) {
      if (property.getName().startsWith(input)) {
        result.add(property);
      }
    }

    return result;
  }

  static private boolean positionInsideContent(String content, Position position) {
    String[] lines = content.split("\n");
    return lines.length > position.getLine()
        && lines[position.getLine()].length() >= position.getCharacter();
  }

}
