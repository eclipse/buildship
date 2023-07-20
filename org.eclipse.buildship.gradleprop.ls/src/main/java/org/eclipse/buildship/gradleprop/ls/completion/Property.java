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

import java.util.List;

/**
 * Is a template of gradle property for jackson databind. Setters and getters should be for the
 * inner method of jackson lib.
 *
 * @author Nikolai Vladimirov
 */
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
