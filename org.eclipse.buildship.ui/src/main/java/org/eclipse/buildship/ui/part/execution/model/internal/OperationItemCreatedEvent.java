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

import org.eclipse.buildship.core.event.GradleEvent;
import org.eclipse.buildship.ui.part.execution.listener.ExecutionTestProgressListener;
import org.eclipse.buildship.ui.part.execution.model.OperationItem;

/**
 * This event is fired, when new ExecutionItems are created and added.
 * 
 * @see ExecutionTestProgressListener
 */
public class OperationItemCreatedEvent implements GradleEvent<OperationItem> {

    private Object source;
    private OperationItem progressItem;

    public OperationItemCreatedEvent(Object source, OperationItem progressItem) {
        this.source = source;
        this.progressItem = progressItem;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public OperationItem getElement() {
        return progressItem;
    }
}
