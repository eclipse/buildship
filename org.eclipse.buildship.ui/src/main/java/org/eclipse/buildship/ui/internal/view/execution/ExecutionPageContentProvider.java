/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.view.execution;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for {@link ExecutionPage}.
 */
public class ExecutionPageContentProvider implements ITreeContentProvider {

    @Override
    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    @Override
    public Object[] getChildren(Object parent) {
        return parent instanceof OperationItem ? ((OperationItem)parent).getChildren().toArray() : new Object[0];
    }

    @Override
    public Object getParent(Object element) {
        return element instanceof OperationItem ? ((OperationItem)element).getParent() : null;
    }

    @Override
    public boolean hasChildren(Object element) {
        return element instanceof OperationItem ? !((OperationItem)element).getChildren().isEmpty() : false;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }
}
