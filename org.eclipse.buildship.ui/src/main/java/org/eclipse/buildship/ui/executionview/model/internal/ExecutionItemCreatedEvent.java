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

package org.eclipse.buildship.ui.executionview.model.internal;

import org.eclipse.buildship.core.event.GradleEvent;
import org.eclipse.buildship.ui.executionview.listener.ExecutionViewTestProgressListener;
import org.eclipse.buildship.ui.executionview.model.ExecutionItem;

/**
 * This event is fired, when new ExecutionItems are created and added.
 * 
 * @see ExecutionViewTestProgressListener
 */
public class ExecutionItemCreatedEvent implements GradleEvent<ExecutionItem> {

    private Object source;
    private ExecutionItem progressItem;

    public ExecutionItemCreatedEvent(Object source, ExecutionItem progressItem) {
        this.source = source;
        this.progressItem = progressItem;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public ExecutionItem getElement() {
        return progressItem;
    }
}
