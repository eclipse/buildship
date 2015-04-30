/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.part.execution.model;

import org.gradle.tooling.events.ProgressEvent;

import org.eclipse.buildship.ui.part.execution.ExecutionPart;
import org.eclipse.buildship.ui.part.execution.model.internal.DefaultOperationItemConfigurator;

/**
 * <p>
 * Implementations of this interface are used to configure {@link OperationItem} instances, which
 * are shown in the {@link ExecutionPart}.
 * </p>
 * <p>
 * Clients can offer a {@link OperationItemConfigurator} as an adapter of a {@link ProgressEvent}.<br/>
 * So before the {@link DefaultOperationItemConfigurator} is used, the {@link ProgressEvent} is
 * asked for an adapter for the {@link OperationItemConfigurator} type.<br/>
 *
 * <pre>
 * <code>OperationItemConfigurator executionItemConfigurator = (OperationItemConfigurator) Platform
 * .getAdapterManager().getAdapter(event, OperationItemConfigurator.class);</code>
 * </pre>
 *
 * By using the <code>org.eclipse.core.runtime.adapters</code> extension point, you can offer a
 * custom implementation of the {@link OperationItemConfigurator} according to the given
 * {@link ProgressEvent}.
 * </p>
 *
 * @see DefaultOperationItemConfigurator
 */
public interface OperationItemConfigurator {

    void configure(OperationItem progressItem);
}
