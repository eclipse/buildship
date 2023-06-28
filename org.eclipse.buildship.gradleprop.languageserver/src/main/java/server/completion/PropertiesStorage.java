package server.completion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class PropertiesStorage {

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
      System.err.println(e.getMessage());
    }
    return propertiesFromFile;
  }

  static private final List<Property> properties = initializeFromFile();

  public static List<Property> getProperties() {
    return properties;
  }
}
