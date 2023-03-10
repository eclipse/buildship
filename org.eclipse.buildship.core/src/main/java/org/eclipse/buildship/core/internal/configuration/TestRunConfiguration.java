/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.util.List;

/**
 * Configuration to launch tasks and tests.
 *
 * @author Donat Csikos
 */
public interface TestRunConfiguration extends BaseRunConfiguration {

    List<Test> getTests();
}
