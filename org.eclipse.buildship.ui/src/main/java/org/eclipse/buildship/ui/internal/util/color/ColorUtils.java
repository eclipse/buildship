/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.internal.util.color;

import com.google.common.base.Preconditions;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

/**
 * Contains helper methods related to colors.
 */
public final class ColorUtils {

    private static final String DECORATIONS_COLOR = "DECORATIONS_COLOR";

    private ColorUtils() {
    }

    /**
     * Returns the color for {@code DECORATIONS_COLOR} from the current workbench theme.
     *
     * @return the theme color to decorate text or null if none is registered.
     */
    public static Color getDecorationsColorFromCurrentTheme() {
        ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        return theme.getColorRegistry().get(DECORATIONS_COLOR);
    }

    /**
     * Returns the color descriptor for {@code DECORATIONS_COLOR} from the current workbench theme.
     *
     * @return the theme color descriptor to decorate text
     */
    public static ColorDescriptor getDecorationsColorDescriptorFromCurrentTheme() {
        ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        return Preconditions.checkNotNull(theme.getColorRegistry().getColorDescriptor(DECORATIONS_COLOR));
    }

}
