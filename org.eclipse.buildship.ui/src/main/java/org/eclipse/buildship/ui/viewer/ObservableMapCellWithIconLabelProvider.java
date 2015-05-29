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

import org.eclipse.buildship.ui.util.color.ColorUtils;
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
    public StyledString getStyledText(Object element) {
        Object label = this.attributeMaps[0].get(element);
        String cellContent = label == null ? "" : label.toString(); //$NON-NLS-1$
        StyledString result = new StyledString(cellContent);

        // if the task contains the text UP-TO-DATE, then display it with a different color
        String upToDate = "UP-TO-DATE";
        int upToDateIndex = cellContent.indexOf(upToDate);
        if (upToDateIndex >= 0) {
            Styler styler = new Styler() {

                @Override
                public void applyStyles(TextStyle textStyle) {
                    textStyle.foreground = ColorUtils.getDecorationsColorFromCurrentTheme();
                }
            };
            result.setStyle(upToDateIndex, upToDate.length(), styler);
        }
        return result;
    }

    @Override
    public Image getImage(Object element) {
        Object imageDescriptor = this.attributeMaps[1].get(element);
        if (imageDescriptor instanceof ImageDescriptor) {
            return this.resourceManager.createImage((ImageDescriptor) imageDescriptor);
        } else {
            return null;
        }
    }

    @Override
    public void dispose() {
        this.resourceManager.dispose();
        super.dispose();
    }

}
