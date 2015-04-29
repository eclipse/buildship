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

package org.eclipse.buildship.ui.view.executionview.listener;

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
import org.eclipse.buildship.ui.view.executionview.ExecutionPart;
import org.eclipse.buildship.ui.view.executionview.model.ExecutionItem;
import org.eclipse.buildship.ui.view.executionview.model.ExecutionItemConfigurator;
import org.eclipse.buildship.ui.view.executionview.model.internal.DefaultExecutionItemConfigurator;
import org.eclipse.buildship.ui.view.executionview.model.internal.ExecutionItemCreatedEvent;

/**
 * This class listens to {@link TestProgressEvent} events, which are send by the Gradle tooling API.
 * It creates appropriate {@link ExecutionItem} objects, which are shown in the
 * {@link ExecutionPart}, according to the incoming events.
 *
 */
public class ExecutionViewTestProgressListener implements TestProgressListener {

    private Map<OperationDescriptor, ExecutionItem> executionItemMap = Maps.newLinkedHashMap();

    private ExecutionItem root;

    private DefaultExecutionItemConfigurator executionItemConfigurator;

    private AtomicBoolean testExecutionItemCreated = new AtomicBoolean();

    public ExecutionViewTestProgressListener(ExecutionItem root) {
        this.root = root;
    }

    @Override
    public void statusChanged(TestProgressEvent event) {
        if (!testExecutionItemCreated.getAndSet(true)) {
            List<ExecutionItem> buildStartedChildren = Lists.newArrayList();
            ExecutionItem tests = new ExecutionItem(null, "Tests");
            buildStartedChildren.add(tests);
            root.setChildren(buildStartedChildren);

            // The new root will be the tests
            root = tests;
        }

        TestOperationDescriptor descriptor = event.getDescriptor();
        ExecutionItem executionItem = executionItemMap.get(descriptor);
        if (null == executionItem) {
            executionItem = new ExecutionItem(descriptor);
            executionItemMap.put(descriptor, executionItem);
            CorePlugin.eventBus().post(new ExecutionItemCreatedEvent(this, executionItem));
        }
        // set the last progress event, so that this can be obtained from the viewers selection
        executionItem.setLastProgressEvent(event);

        // Configure progressItem according to the given event
        ExecutionItemConfigurator executionItemConfigurator = (ExecutionItemConfigurator) Platform.getAdapterManager().getAdapter(event, ExecutionItemConfigurator.class);
        if (null == executionItemConfigurator) {
            executionItemConfigurator = getDefaultProgressItemConfigurator(event);
        }
        executionItemConfigurator.configure(executionItem);

        // attach to parent, if necessary
        ExecutionItem parentExecutionItem = getParent(descriptor);
        if (!parentExecutionItem.getChildren().contains(executionItem)) {
            List<ExecutionItem> children = Lists.newArrayList(parentExecutionItem.getChildren());
            children.add(executionItem);
            parentExecutionItem.setChildren(children);
        }
    }

    protected ExecutionItemConfigurator getDefaultProgressItemConfigurator(ProgressEvent propressEvent) {
        if (null == executionItemConfigurator) {
            executionItemConfigurator = new DefaultExecutionItemConfigurator();
        }
        executionItemConfigurator.setPropressEvent(propressEvent);
        return executionItemConfigurator;
    }

    protected ExecutionItem getParent(OperationDescriptor descriptor) {
        OperationDescriptor parent = descriptor.getParent();
        ExecutionItem parentExecutionItem = executionItemMap.get(parent);
        if (null == parentExecutionItem) {
            parentExecutionItem = root;
        }
        return parentExecutionItem;
    }
}
