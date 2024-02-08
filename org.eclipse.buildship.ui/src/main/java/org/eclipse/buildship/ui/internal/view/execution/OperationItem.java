/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution;

import java.util.List;

import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.StartEvent;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.ui.internal.view.ObservableItem;

/**
 * <p>
 * {@code OperationItem} instances are the nodes added to the trees of the
 * {@link org.eclipse.buildship.ui.internal.view.execution.ExecutionsView}.
 * </p>
 * <pre>
 * ISelection selection = HandlerUtil.getCurrentSelection(event);
 * if (selection instanceof IStructuredSelection) {
 *     IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 *     Object firstElement = structuredSelection.getFirstElement();
 *     if (firstElement instanceof IAdaptable) {
 *         IAdaptable adaptable = (IAdaptable) firstElement;
 *         OperationDescriptor adapter = (OperationDescriptor) adaptable.getAdapter(OperationDescriptor.class);
 *         // ... do something with the OperationDescriptor
 *     }
 * }
 * </pre>
 */
public final class OperationItem extends ObservableItem implements IAdaptable {

    private final StartEvent startEvent;
    private final OperationDescriptor descriptor;
    private FinishEvent finishEvent;
    private String name;
    private OperationItem parent;
    private List<OperationItem> children;

    public OperationItem() {
        this.startEvent = null;
        this.descriptor = null;
        this.finishEvent = null;
        this.name = null;
        this.children = Lists.newArrayList();
    }

    public OperationItem(ProgressEvent event) {
        this.startEvent = event instanceof StartEvent ? (StartEvent)event : null;
        this.descriptor = event.getDescriptor();
        this.finishEvent = null;
        this.name = event.getDescriptor().getDisplayName();
        this.children = Lists.newArrayList();
    }

    public StartEvent getStartEvent() {
        return this.startEvent;
    }

    public OperationDescriptor getDescriptor() {
        return this.descriptor;
    }

    public FinishEvent getFinishEvent() {
        return this.finishEvent;
    }

    public void setFinishEvent(FinishEvent finishEvent) {
        this.finishEvent = finishEvent;
    }

    public String getName() {
        return this.name;
    }

    public List<OperationItem> getChildren() {
        return ImmutableList.copyOf(this.children);
    }

    public OperationItem getParent() {
        return this.parent;
    }

    public void addChild(OperationItem operationItem) {
        if (!this.children.contains(operationItem)) {
            List<OperationItem> children = Lists.newArrayList(this.children);
            children.add(operationItem);
            setChildren(children);
        }
    }

    public void removeChild(OperationItem operationItem) {
        if (this.children.contains(operationItem)) {
            List<OperationItem> children = Lists.newArrayList(this.children);
            children.remove(operationItem);
            setChildren(children);
        }
    }

    private void setChildren(List<OperationItem> children) {
        for (OperationItem child : children) {
            child.parent = this;
        }
        this.children = children;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object getAdapter(Class adapter) {
        if (OperationDescriptor.class.equals(adapter)) {
            return this.getDescriptor();
        } else {
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }
    }

}
