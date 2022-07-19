/*******************************************************************************
 * Copyright (c) 2022 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.model.internal;

import java.io.File;
import java.io.Serializable;
import java.util.List;


public class DefaultProject implements Serializable {

    private static final long serialVersionUID = 1L;
    private final File location;
    private final List<String> sourceSetNames;
    private final DefaultCompileJavaTaskConfiguration compileJavaTaskConfiguration;

    public DefaultProject(File location,
                          List<String> sourceSetNames,
                          DefaultCompileJavaTaskConfiguration compileJavaTaskConfiguration) {
        this.location = location;
        this.sourceSetNames = sourceSetNames;
        this.compileJavaTaskConfiguration = compileJavaTaskConfiguration;
    }

    public File getLocation() {
        return this.location;
    }

    public List<String> getSourceSetNames() {
        return this.sourceSetNames;
    }

    public DefaultCompileJavaTaskConfiguration getCompileJavaTaskConfiguration() {
        return this.compileJavaTaskConfiguration;
    }
}
