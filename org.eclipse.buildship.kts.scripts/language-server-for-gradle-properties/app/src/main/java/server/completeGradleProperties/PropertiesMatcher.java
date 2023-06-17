package server.completeGradleProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

public class PropertiesMatcher {

  public static List<CompletionItem> getCompletions(String content, Position position) {
    var completionWord = getCompletionWord(content, position);
    char lastSymbol = '\0';
    if (!completionWord.isEmpty()) {
      lastSymbol = completionWord.charAt(completionWord.length() - 1);
    }
    var onlyValues = false;

    // if it's true, we consider only values
    if (lastSymbol == '=') {
      completionWord = completionWord.substring(0, completionWord.length() - 1);
      onlyValues = true;
    }

    if (onlyValues) {
      List<CompletionItem> result = new ArrayList<>();
      for (var property : StorageGradleProperties.getProperties()) {
        if (property.getName().equals(completionWord)) {
          for (var value : property.getValues()) {
            result.add(new CompletionItem(value));
          }
        }
      }
      return result;
    }

    var matchedProperties = getMatchedProperties(completionWord);

    return matchedProperties.stream()
        .map(property -> {
          CompletionItem item = new CompletionItem();
          item.setLabel(property.getName());
          item.setDocumentation(property.getDescription());
          return item;
        })
        .collect(Collectors.toList());
  }

  private static String getCompletionWord(String content, Position position) {
    var lines = content.split("\n");
    if (position.getCharacter() == 0) {
      return "";
    }
    var workLine = lines[position.getLine()].substring(0, position.getCharacter());
    var wordsOnLine = workLine.split("\\s+");
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
    var properties = StorageGradleProperties.getProperties();

    for (var property : properties) {
      if (property.getName().startsWith(input)) {
        result.add(property);
      }
    }

    return result;
  }
}
