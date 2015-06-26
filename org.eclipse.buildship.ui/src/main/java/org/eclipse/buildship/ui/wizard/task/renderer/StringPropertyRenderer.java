/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.wizard.task.renderer;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * {@link PropertyRenderer}, which is used to render a {@link Text} control.
 *
 */
public class StringPropertyRenderer implements PropertyRenderer {

    private Text propertyValue;

    @Override
    public void createControl(Composite parent) {
        propertyValue = new Text(parent, SWT.BORDER);
        propertyValue.setMessage("Set the value of this property");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(propertyValue);
    }

    @Override
    public Control getControl() {
        return propertyValue;
    }

    @Override
    public IObservableValue getObservable() {
        return WidgetProperties.text(SWT.Modify).observe(propertyValue);
    }

    @Override
    public UpdateValueStrategy getTargetUpdateValueStrategy() {
        // Not necessary here
        return null;
    }

    @Override
    public UpdateValueStrategy getModelUpdateValueStrategy() {
        // Not necessary here
        return null;
    }

}
