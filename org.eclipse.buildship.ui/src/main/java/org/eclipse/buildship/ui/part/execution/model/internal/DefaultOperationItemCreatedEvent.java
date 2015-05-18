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

package org.eclipse.buildship.ui.part.execution.model.internal;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.ui.part.execution.model.OperationItem;
import org.eclipse.buildship.ui.part.execution.model.OperationItemCreatedEvent;

/**
 * Implementation of {@link OperationItemCreatedEvent}.
 */
public final class DefaultOperationItemCreatedEvent implements OperationItemCreatedEvent {

    private final Object source;
    private final OperationItem operationItem;

    public DefaultOperationItemCreatedEvent(Object source, OperationItem operationItem) {
        this.source = Preconditions.checkNotNull(source);
        this.operationItem = Preconditions.checkNotNull(operationItem);
    }

    @Override
    public Object getSource() {
        return this.source;
    }

    @Override
    public OperationItem getElement() {
        return this.operationItem;
    }
}
