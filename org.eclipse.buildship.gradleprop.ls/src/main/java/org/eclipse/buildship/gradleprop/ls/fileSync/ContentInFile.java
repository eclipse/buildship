/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.ls.fileSync;

/**
 * Describes content inside a file as content itself and its version.
 *
 * @author Nikolai Vladimirov
 */
public class ContentInFile {

  private String content;
  private int version;

  public ContentInFile(String content, int version) {
    this.content = content;
    this.version = version;
  }

  public void updateFile(String newContent, int newVersion) {
    content = newContent;
    version = newVersion;
  }

  public String getContent() {
    return content;
  }

  public int getVersion() {
    return version;
  }
}
