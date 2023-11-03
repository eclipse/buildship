/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
    private static final String ERROR_COLOR = "ERROR_COLOR";

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

    /**
     * Returns the color descriptor for {@code ERROR_COLOR} from the current workbench theme.
     *
     * @return the theme color descriptor to decorate text
     */
    public static ColorDescriptor getErrorColorDescriptorFromCurrentTheme() {
        ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        return Preconditions.checkNotNull(theme.getColorRegistry().getColorDescriptor(ERROR_COLOR));
    }
}
