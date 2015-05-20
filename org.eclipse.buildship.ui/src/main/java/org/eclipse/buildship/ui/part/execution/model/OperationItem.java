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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.eclipse.buildship.ui.view.ObservableItem;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.gradle.tooling.events.OperationDescriptor;

import java.util.List;

/**
 * <p>
 * {@code OperationItem} instances are the nodes added to the trees of the
 * {@link org.eclipse.buildship.ui.view.execution.ExecutionsView}.
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
 * Note that all of the current APIs are required since they are called reflectively
 * by the Eclipse Data Binding framework.
 */
@SuppressWarnings("unchecked")
public final class OperationItem extends ObservableItem implements IAdaptable {

    public static final String FIELD_NAME = "name";         //$NON-NLS-1$
    public static final String FIELD_DURATION = "duration"; //$NON-NLS-1$
    public static final String FIELD_IMAGE = "image";       //$NON-NLS-1$
    public static final String FIELD_CHILDREN = "children"; //$NON-NLS-1$

    private final OperationDescriptor operationDescriptor;
    private String name;
    private String duration;
    private ImageDescriptor image;
    private List<OperationItem> children;

    public OperationItem() {
        this.operationDescriptor = null;
        this.name = null;
        this.duration = null;
        this.image = null;
        this.children = Lists.newArrayList();
    }

    public OperationItem(OperationDescriptor operationDescriptor) {
        this.operationDescriptor = Preconditions.checkNotNull(operationDescriptor);
        this.name = operationDescriptor.getDisplayName();
        this.duration = null;
        this.image = null;
        this.children = Lists.newArrayList();
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        firePropertyChange(FIELD_NAME, this.name, this.name = name);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getDuration() {
        return this.duration;
    }

    public void setDuration(String duration) {
        firePropertyChange(FIELD_DURATION, this.duration, this.duration = duration);
    }

    @SuppressWarnings("UnusedDeclaration")
    public ImageDescriptor getImage() {
        return this.image;
    }

    public void setImage(ImageDescriptor image) {
        firePropertyChange(FIELD_IMAGE, this.image, this.image = image);
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<OperationItem> getChildren() {
        return ImmutableList.copyOf(this.children);
    }

    public void addChild(OperationItem operationItem) {
        if (!this.children.contains(operationItem)) {
            List<OperationItem> children = Lists.newArrayList(this.children);
            children.add(operationItem);
            setChildren(children);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void removeChild(OperationItem operationItem) {
        if (this.children.contains(operationItem)) {
            List<OperationItem> children = Lists.newArrayList(this.children);
            children.remove(operationItem);
            setChildren(children);
        }
    }

    private void setChildren(List<OperationItem> children) {
        firePropertyChange(FIELD_CHILDREN, this.children, this.children = children);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        if (OperationDescriptor.class.equals(adapter)) {
            return this.operationDescriptor;
        } else {
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }
    }

}
