package server.completeGradleProperties;

import java.util.List;

public class Property {

  public String propertyName;
  public List<String> values;

  public Property(String name, List<String> values) {
    this.propertyName = name;
    this.values = values;
  }
}
