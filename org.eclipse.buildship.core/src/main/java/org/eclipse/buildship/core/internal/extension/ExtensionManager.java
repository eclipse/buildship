/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.extension;

import java.util.List;

import org.eclipse.buildship.core.invocation.InvocationCustomizer;

/**
 * Loads contributions from the extension registry.
 *
 * <p>
 * The extension registry implementation hides all registration details, so there's no
 * straightforward way to add custom contributions programmatically. Without this extra
 * service interface we wouldn't be able to add extensive integration test coverage
 * in isolation (defining test configurators in fragment.xml would affect existing tests).
 *
 * @author Donat Csikos
 */
public interface ExtensionManager {

    List<ProjectConfiguratorContribution> loadConfigurators();

    List<InvocationCustomizer> loadCustomizers();
}
