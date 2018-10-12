/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.extension;

import java.util.List;

import org.eclipse.buildship.core.ProjectConfigurator;
import org.eclipse.buildship.core.invocation.InvocationCustomizer;

/**
 * Loads contributions from the extension registry.
 * <p>
 * The extension registry implementation hides all registration details, so there's no
 * straightforward way to add custom contributions programmatically. Without this extra
 * service interface we wouldn't be able to add extensive integration test coverage
 * in isolation (defining test configurators in fragment.xml would affect existing tests).
 *
 * @author Donat Csikos
 */
public interface ExtensionManager {

    List<ProjectConfigurator> loadConfigurators();

    List<InvocationCustomizer> loadCustomizers();
}
