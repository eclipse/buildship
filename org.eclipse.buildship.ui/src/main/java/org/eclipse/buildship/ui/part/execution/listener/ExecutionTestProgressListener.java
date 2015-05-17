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

package org.eclipse.buildship.ui.part.execution.listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestProgressEvent;
import org.gradle.tooling.events.test.TestProgressListener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.part.execution.model.OperationItem;
import org.eclipse.buildship.ui.part.execution.model.OperationItemConfigurator;
import org.eclipse.buildship.ui.part.execution.model.internal.DefaultOperationItemConfigurator;
import org.eclipse.buildship.ui.part.execution.model.internal.DefaultOperationItemCreatedEvent;

/**
 * This class listens to {@link TestProgressEvent} events, which are send by the Gradle tooling API.
 * It creates appropriate {@link OperationItem} objects, which are shown in the
 * {@link org.eclipse.buildship.ui.part.execution.ExecutionsView}, according to the incoming events.
 *
 */
public class ExecutionTestProgressListener implements TestProgressListener {

    private Map<OperationDescriptor, OperationItem> executionItemMap = Maps.newLinkedHashMap();

    private OperationItem root;

    private DefaultOperationItemConfigurator executionItemConfigurator;

    private AtomicBoolean testExecutionItemCreated = new AtomicBoolean();

    public ExecutionTestProgressListener(OperationItem root) {
        this.root = root;
    }

    @Override
    public void statusChanged(TestProgressEvent event) {
        if (!this.testExecutionItemCreated.getAndSet(true)) {
            OperationItem tests = new OperationItem(null, "Tests");
            this.root.addChild(tests);

            // The new root will be the tests
            this.root = tests;
        }

        TestOperationDescriptor descriptor = event.getDescriptor();
        OperationItem operationItem = this.executionItemMap.get(descriptor);
        if (null == operationItem) {
            operationItem = new OperationItem(descriptor);
            this.executionItemMap.put(descriptor, operationItem);
            CorePlugin.listenerRegistry().dispatch(new DefaultOperationItemCreatedEvent(this, operationItem));
        }
        // set the last progress event, so that this can be obtained from the viewers selection
        operationItem.setLastProgressEvent(event);

        // Configure progressItem according to the given event
        @SuppressWarnings("cast")
        OperationItemConfigurator operationItemConfigurator = (OperationItemConfigurator) Platform.getAdapterManager().getAdapter(event, OperationItemConfigurator.class);
        if (null == operationItemConfigurator) {
            operationItemConfigurator = getDefaultProgressItemConfigurator(event);
        }
        operationItemConfigurator.configure(operationItem);

        // attach to parent, if necessary
        OperationItem parentExecutionItem = getParent(descriptor);
        if (!parentExecutionItem.getChildren().contains(operationItem)) {
            List<OperationItem> children = Lists.newArrayList(parentExecutionItem.getChildren());
            children.add(operationItem);
            parentExecutionItem.setChildren(children);
        }
    }

    protected OperationItemConfigurator getDefaultProgressItemConfigurator(ProgressEvent propressEvent) {
        if (null == this.executionItemConfigurator) {
            this.executionItemConfigurator = new DefaultOperationItemConfigurator();
        }
        this.executionItemConfigurator.setPropressEvent(propressEvent);
        return this.executionItemConfigurator;
    }

    protected OperationItem getParent(OperationDescriptor descriptor) {
        OperationDescriptor parent = descriptor.getParent();
        OperationItem parentExecutionItem = this.executionItemMap.get(parent);
        if (null == parentExecutionItem) {
            parentExecutionItem = this.root;
        }
        return parentExecutionItem;
    }
}
