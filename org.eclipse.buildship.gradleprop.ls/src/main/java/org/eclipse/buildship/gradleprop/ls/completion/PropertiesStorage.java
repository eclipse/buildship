package org.eclipse.buildship.gradleprop.ls.completion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesStorage {

  private static Logger LOGGER = LoggerFactory.getLogger(PropertiesStorage.class);

  private static List<Property> initializeFromFile() {
    List<Property> propertiesFromFile = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    try {
      InputStream resource = PropertiesStorage.class.getClassLoader()
          .getResourceAsStream("properties.json");
      propertiesFromFile = mapper.readValue(resource,
          new TypeReference<List<Property>>() {
          });
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
    return propertiesFromFile;
  }

  static private final List<Property> properties = initializeFromFile();

  public static List<Property> getProperties() {
    return properties;
  }
}
