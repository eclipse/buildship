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

import com.google.common.collect.ImmutableMap;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import java.util.Map;

/**
 * This ObservableMapCellLabelProvider implementation also supports an image for a
 * {@link org.eclipse.jface.viewers.ViewerCell}.
 */
public class ObservableMapCellWithIconLabelProvider extends ObservableMapCellLabelProvider implements IStyledLabelProvider {

    private final ImmutableMap<String, ColorDescriptor> customTextColoringMapping;
    private final ResourceManager resourceManager;

    public ObservableMapCellWithIconLabelProvider(Map<String, ColorDescriptor> customTextColoringMapping, IObservableMap... attributeMaps) {
        super(attributeMaps);
        this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
        this.customTextColoringMapping = ImmutableMap.copyOf(customTextColoringMapping);
    }

    @Override
    public StyledString getStyledText(Object element) {
        Object label = this.attributeMaps[0].get(element);
        String rawLabel = label == null ? "" : label.toString(); //$NON-NLS-1$
        StyledString styledLabel = new StyledString(rawLabel);

        // apply custom coloring of those parts of the label for which there is a custom coloring mapping
        for (String text : this.customTextColoringMapping.keySet()) {
            assignColorToText(rawLabel, styledLabel, text);
        }

        return styledLabel;
    }

    private void assignColorToText(String rawLabel, StyledString styledLabel, final String text) {
        int index = rawLabel.indexOf(text);
        if (index >= 0) {
            Styler styler = new Styler() {

                @Override
                public void applyStyles(TextStyle textStyle) {
                    ColorDescriptor substringColorDescriptor = ObservableMapCellWithIconLabelProvider.this.customTextColoringMapping.get(text);
                    Color substringColor = (Color) ObservableMapCellWithIconLabelProvider.this.resourceManager.find(substringColorDescriptor);
                    if (substringColor == null) {
                        substringColor = ObservableMapCellWithIconLabelProvider.this.resourceManager.createColor(substringColorDescriptor);
                    }
                    textStyle.foreground = substringColor;
                }
            };
            styledLabel.setStyle(index, text.length(), styler);
        }
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
