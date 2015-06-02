/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.util.progress;

/**
 * A command that runs a Tooling API operation.
 */
public interface ToolingApiCommand {

    /**
     * Runs a Tooling API operation.
     *
     * @throws Exception thrown if running the operation fails
     */
    void run() throws Exception;

}
