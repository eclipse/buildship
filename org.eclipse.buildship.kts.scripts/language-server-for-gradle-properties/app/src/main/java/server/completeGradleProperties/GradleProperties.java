package server.completeGradleProperties;

import java.util.ArrayList;
import java.util.List;


public class GradleProperties {

  static public String[] properties = new String[]{
      "org.gradle.caching",
      "org.gradle.caching.debug",
      "org.gradle.configureondemand",
      "org.gradle.console",
      "org.gradle.continuous.quietperiod",
      "org.gradle.daemon",
      "org.gradle.daemon.idletimeout",
      "org.gradle.debug",
      "org.gradle.debug.host",
      "org.gradle.debug.port",
      "org.gradle.debug.server",
      "org.gradle.debug.suspend",
      "org.gradle.java.home",
      "org.gradle.jvmargs",
      "org.gradle.logging.level",
      "org.gradle.logging.stacktrace",
      "org.gradle.parallel",
      "org.gradle.priority",
      "org.gradle.vfs.verbose",
      "org.gradle.vfs.watch",
      "org.gradle.warning.mode",
      "org.gradle.welcome",
      "org.gradle.workers.max"
  };

  static public List<String> matchedProperties(String input) {
    List<String> result = new ArrayList<>();
    if (input.isEmpty()) {
      return result;
    }
    for (var property : properties) {
      if (property.startsWith(input)) {
        result.add(property);
      }
    }
    return result;
  }
}
