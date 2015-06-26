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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Renderer for the properties, which are shown in the
 * {@link org.eclipse.buildship.ui.wizard.task.CreateTaskTypeWizardPropertiesPage}.
 *
 */
public interface PropertyRenderer {

    void createControl(Composite parent);

    Control getControl();

    IObservableValue getObservable();

    UpdateValueStrategy getTargetUpdateValueStrategy();

    UpdateValueStrategy getModelUpdateValueStrategy();
}
