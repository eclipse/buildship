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

package org.eclipse.buildship.ui.view.execution;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.part.execution.model.OperationItem;
import org.eclipse.buildship.ui.part.execution.model.OperationItemConfigurator;
import org.eclipse.buildship.ui.part.execution.model.internal.DefaultOperationItemConfigurator;
import org.eclipse.buildship.ui.part.execution.model.internal.DefaultOperationItemCreatedEvent;
import org.eclipse.core.runtime.Platform;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.ProgressEvent;

import java.util.List;
import java.util.Map;

/**
 * Listens to {@link org.gradle.tooling.events.ProgressEvent} instances that are sent by the Tooling API while a build is executed. Each
 * incoming event is added to the execution tree as an {@link org.eclipse.buildship.ui.part.execution.model.OperationItem} instance.
 */
public final class ExecutionProgressListener implements org.gradle.tooling.events.ProgressListener {

    private final OperationItem root;
    private final DefaultOperationItemConfigurator executionItemConfigurator;
    private final Map<OperationDescriptor, OperationItem> executionItemMap = Maps.newLinkedHashMap();

    public ExecutionProgressListener(OperationItem root) {
        this.root = root;
        this.executionItemConfigurator = new DefaultOperationItemConfigurator();
    }

    @Override
    public void statusChanged(ProgressEvent progressEvent) {
        OperationDescriptor descriptor = progressEvent.getDescriptor();
        OperationItem operationItem = this.executionItemMap.get(descriptor);
        boolean createdNewOperationItem = false;
        if (null == operationItem) {
            operationItem = new OperationItem(descriptor);
            this.executionItemMap.put(descriptor, operationItem);
            createdNewOperationItem = true;
        }
        // set the last progress event, so that this can be obtained from the viewers selection
        operationItem.setLastProgressEvent(progressEvent);

        // Configure OperationItem according to the given event
        @SuppressWarnings({"cast", "RedundantCast"})
        OperationItemConfigurator operationItemConfigurator = (OperationItemConfigurator) Platform.getAdapterManager().getAdapter(progressEvent, OperationItemConfigurator.class);
        if (operationItemConfigurator == null) {
            this.executionItemConfigurator.setProgressEvent(progressEvent);
            operationItemConfigurator = this.executionItemConfigurator;
        }
        operationItemConfigurator.configure(operationItem);

        // attach to parent, if necessary
        OperationItem parentExecutionItem = getParent(descriptor);
        if (!parentExecutionItem.getChildren().contains(operationItem)) {
            List<OperationItem> children = Lists.newArrayList(parentExecutionItem.getChildren());
            children.add(operationItem);
            parentExecutionItem.setChildren(children);
        }

        if (createdNewOperationItem) {
            CorePlugin.listenerRegistry().dispatch(new DefaultOperationItemCreatedEvent(this, parentExecutionItem));
        }
    }

    protected OperationItem getParent(OperationDescriptor descriptor) {
        OperationDescriptor parent = descriptor.getParent();
        OperationItem parentExecutionItem = this.executionItemMap.get(parent);
        if (parentExecutionItem == null) {
            parentExecutionItem = this.root;
        }
        return parentExecutionItem;
    }

}
