/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

import java.util.Map;

/**
 * Describes a build command in an Eclipse project.
 *
 * @author Donát Csikós
 */
public interface OmniEclipseBuildCommand {

    /**
     * Returns the name of the build command.
     *
     * @return the name of the build command
     */
    String getName();

    /**
     * Returns the arguments supplied for the build command.
     *
     * @return the build command arguments
     */
    Map<String, String> getArguments();

}
