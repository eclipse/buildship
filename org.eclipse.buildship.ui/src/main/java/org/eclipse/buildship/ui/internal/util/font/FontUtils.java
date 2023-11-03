/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.font;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Contains helper methods related to fonts.
 */
public final class FontUtils {

    private FontUtils() {
    }

    /**
     * Provides a new font derived from {@link JFaceResources#getDialogFont()}, amended with the
     * selected modifiers.
     * <p>
     * The combination of the following style bits can be used: {@link org.eclipse.swt.SWT#NORMAL}, {@link org.eclipse.swt.SWT#BOLD}
     * and {@link org.eclipse.swt.SWT#ITALIC}.
     *
     * @param style the requested font style
     * @return the custom dialog font
     */
    public static synchronized Font getCustomDialogFont(int style) {
        Font defaultFont = JFaceResources.getDialogFont();
        Device device = defaultFont.getDevice();
        FontData[] fontData = defaultFont.getFontData();
        FontData derivedFontData = new FontData(fontData[0].getName(), fontData[0].getHeight(), style);
        derivedFontData.setLocale(fontData[0].getLocale());
        return new Font(device, derivedFontData);
    }

    /**
     * Returns the height of the target font in pixels.
     *
     * @param font the target font
     * @return the height of the font
     */
    public static int getFontHeightInPixels(Font font) {
        return font.getFontData()[0].getHeight();
    }

}
