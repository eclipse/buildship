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
 * Loads contributions from extension registry.
 * <p>
 * The extension registry API is very conservative and provides only read operations. In order to
 * to test possible contribution scenarios we need this extra level of indirection.
 *
 * @author Donat Csikos
 */
public interface ExtensionManager {

    List<ProjectConfigurator> loadConfigurators();

    List<InvocationCustomizer> loadCustomizers();
}
