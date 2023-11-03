/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.ls.completion;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialises and gives the list of gradle properties.
 *
 * @author Nikolai Vladimirov
 */
public class PropertiesStorage {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesStorage.class);

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
