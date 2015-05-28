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

package org.eclipse.buildship.ui.viewer;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;

/**
 * This ObservableMapCellLabelProvider implementation also supports an image for a
 * {@link org.eclipse.jface.viewers.ViewerCell}.
 */
public class ObservableMapCellWithIconLabelProvider extends ObservableMapCellLabelProvider implements IStyledLabelProvider {

    private final ResourceManager resourceManager;

    public ObservableMapCellWithIconLabelProvider(IObservableMap... attributeMaps) {
        super(attributeMaps);
        this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public void dispose() {
        super.dispose();
        this.resourceManager.dispose();
    }

    @Override
    public StyledString getStyledText(Object element) {
        Object value = this.attributeMaps[0].get(element);
        String cellContent = value == null ? "" : value.toString(); //$NON-NLS-1$
        StyledString result = new StyledString(cellContent);

        // if the task contains the text UP-TO-DATE, then display it with a different color
        String upToDate = "UP-TO-DATE";
        int upToDateIndex = cellContent.indexOf(upToDate);
        if (upToDateIndex >= 0) {
            Styler styler = new Styler() {

                @Override
                public void applyStyles(TextStyle textStyle) {
                    textStyle.foreground = WorkbenchUtils.getDecorationsColorFromCurrentTheme();
                }
            };
            result.setStyle(upToDateIndex, upToDate.length(), styler);
        }
        return result;
    }

    @Override
    public Image getImage(Object element) {
        Object value = this.attributeMaps[1].get(element);
        if (value instanceof ImageDescriptor) {
            Image image = this.resourceManager.createImage((ImageDescriptor) value);
            return image;
        }
        return null;
    }

}
