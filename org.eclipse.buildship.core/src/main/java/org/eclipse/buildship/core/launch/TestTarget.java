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

package org.eclipse.buildship.core.launch;

import com.gradleware.tooling.toolingclient.TestConfig;

/**
 * Represents an input item for a Gradle test execution.
 */
public interface TestTarget {

    /**
     * Returns the name of the element.For a class it's the name without the package declaration.
     * 
     * @return the simple name of the element
     */
    String getSimpleName();

    /**
     * Returns the qualified name of the element. For a class its the name containing the package
     * declaration.
     * 
     * @return the qualified name of the element
     */
    String getQualifiedName();

    /**
     * Adds the current element to the target test configuration.
     * 
     * @param testConfig the configuration to apply the current element on
     */
    void apply(TestConfig.Builder testConfig);
}
