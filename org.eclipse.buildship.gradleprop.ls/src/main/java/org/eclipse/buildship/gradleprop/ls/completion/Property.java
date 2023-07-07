package org.eclipse.buildship.gradleprop.ls.completion;

import java.util.List;

public class Property {

  private String name;
  private String description;

  private List<String> values;

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

}
