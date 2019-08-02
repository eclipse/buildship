/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import java.util.List;

/**
 * Configuration to launch tasks and tests.
 *
 * @author Donat Csikos
 */
public interface TestLaunchConfiguration extends BaseLaunchConfiguration {

    List<String> getTestClasses();

    List<String> getTestMethods();
}
