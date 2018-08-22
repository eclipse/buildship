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

package org.eclipse.buildship.ui.internal.view;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Convenience base class for items with properties that need to be observable.
 *
 * Note that all of the current APIs are required since they are called reflectively
 * by the Eclipse Data Binding framework.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class ObservableItem {

    private final PropertyChangeSupport propertyChangeSupport;

    protected ObservableItem() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }

}
