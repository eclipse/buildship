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

package org.eclipse.buildship.ui.view.task;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * {@link DecoratingStyledCellLabelProvider}, which is used to offer appropriate Eclipse elements
 * for the LabelProvider, which should be styled.
 */
public class TaskDecoratingStyledCellLabelProvider extends DecoratingStyledCellLabelProvider {

    public TaskDecoratingStyledCellLabelProvider(IStyledLabelProvider labelProvider, ILabelDecorator decorator, IDecorationContext decorationContext) {
        super(labelProvider, decorator, decorationContext);
    }

    @Override
    protected StyledString getStyledText(Object element) {
        return super.getStyledText(getTaskViewElement(element));
    }

    @Override
    public Image getImage(Object element) {
        return super.getImage(getTaskViewElement(element));
    }

    @Override
    public Color getBackground(Object element) {
        return super.getBackground(getTaskViewElement(element));
    }

    @Override
    public Color getForeground(Object element) {
        return super.getForeground(getTaskViewElement(element));
    }

    @Override
    public Font getFont(Object element) {
        return super.getFont(getTaskViewElement(element));
    }

    protected Object getTaskViewElement(Object element) {
        if (element instanceof ProjectNode) {
            return ((ProjectNode) element).getWorkspaceProject().get();
        }
        return element;
    }
}
