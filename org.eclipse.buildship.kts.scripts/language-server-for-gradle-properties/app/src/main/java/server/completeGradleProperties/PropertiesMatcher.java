package server.completeGradleProperties;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;

public class PropertiesMatcher {

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

  public static List<CompletionItem> getCompletions(String content, Position position) {

    var completionWord = getCompletionWord(content, position);
    var matchedProperties = GradleProperties.matchedProperties(completionWord);

    return matchedProperties.stream()
        .map(property -> {
          CompletionItem item = new CompletionItem();
          item.setLabel(property);
          return item;
        })
        .collect(Collectors.toList());
  }
}
