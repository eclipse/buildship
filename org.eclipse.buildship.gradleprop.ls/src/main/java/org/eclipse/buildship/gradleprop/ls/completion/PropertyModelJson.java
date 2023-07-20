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

/**
 * Is a template for the list of gradle properties. They are placed as an array in the external
 * json.
 *
 * @author Nikolai Vladimirov
 */

public class PropertyModelJson {

  private ArrayList<Property> properties;

  public ArrayList<Property> getProperties() {
    return properties;
  }

  public void setProperties(ArrayList<Property> properties) {
    this.properties = properties;
  }
}
