/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.io.File;

/**
 * Represents a Gradle script. A Gradle script may be a build script, a settings script, or an initialization script.
 *
 * @author Etienne Studer
 */
public interface OmniGradleScript {

    /**
     * Returns the source file for this script, or {@code null} if this script has no associated source file. If this method returns a non-null value, the given source file will
     * exist.
     *
     * @return the source file, null if the script has no associated source file
     */
    File getSourceFile();

}
