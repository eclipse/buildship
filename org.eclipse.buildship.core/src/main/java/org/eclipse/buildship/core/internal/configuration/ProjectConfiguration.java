/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import java.io.File;

/**
 * Configuration for a project in a Gradle build.
 *
 * @author Donat Csikos
 */
public interface ProjectConfiguration {

    File getProjectDir();

    BuildConfiguration getBuildConfiguration();

}
