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

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

/**
 * This ObservableMapCellLabelProvider implementation also supports an image for a
 * {@link org.eclipse.jface.viewers.ViewerCell}.
 */
public class ObservableMapCellWithIconLabelProvider extends ObservableMapCellLabelProvider implements IStyledLabelProvider {

    private final ResourceManager resourceManager;

    private final Map<String, ColorDescriptor> decoratedSubstringColors;

    public ObservableMapCellWithIconLabelProvider(Map<String, ColorDescriptor> decoratedSubstringColors, IObservableMap... attributeMaps) {
        super(attributeMaps);
        this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
        this.decoratedSubstringColors = ImmutableMap.copyOf(decoratedSubstringColors);

        // store the Color instances in the resource manager
        for (String decoratedSubstring : this.decoratedSubstringColors.keySet()) {
            this.resourceManager.create(this.decoratedSubstringColors.get(decoratedSubstring));
        }
    }

    @Override
    public StyledString getStyledText(Object element) {
        Object label = this.attributeMaps[0].get(element);
        String rawLabel = label == null ? "" : label.toString(); //$NON-NLS-1$
        StyledString styledLabel = new StyledString(rawLabel);

        // color all substrings from decoratedSubstringColors in the label
        for (final String substringToColor : this.decoratedSubstringColors.keySet()) {
            assignColorToSubstring(rawLabel, styledLabel, substringToColor);
        }

        return styledLabel;
    }

    private void assignColorToSubstring(String rawLabel, StyledString styledLabel, final String substringToColor) {
        final int substringIndex = rawLabel.indexOf(substringToColor);
        if (substringIndex >= 0) {
            Styler styler = new Styler() {

                @Override
                public void applyStyles(TextStyle textStyle) {
                    ColorDescriptor substringColorDescriptor = ObservableMapCellWithIconLabelProvider.this.decoratedSubstringColors.get(substringToColor);
                    Color substringColor = (Color) ObservableMapCellWithIconLabelProvider.this.resourceManager.find(substringColorDescriptor);
                    textStyle.foreground = substringColor;
                }
            };
            styledLabel.setStyle(substringIndex, substringToColor.length(), styler);
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
