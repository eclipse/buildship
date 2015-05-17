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

package org.eclipse.buildship.ui.viewer.labelprovider;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;

/**
 * This ObservableMapCellLabelProvider implementation also supports an image for a
 * {@link ViewerCell}.
 */
public class ObservableMapCellWithIconLabelProvider extends ObservableMapCellLabelProvider {

    private final ResourceManager resourceManager;

    public ObservableMapCellWithIconLabelProvider(IObservableMap... attributeMaps) {
        super(attributeMaps);
        this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public void update(ViewerCell cell) {
        super.update(cell);
        Object element = cell.getElement();
        Object value = this.attributeMaps[1].get(element);
        if (value instanceof ImageDescriptor) {
            Image image = this.resourceManager.createImage((ImageDescriptor) value);
            cell.setImage(image);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        this.resourceManager.dispose();
    }

}
