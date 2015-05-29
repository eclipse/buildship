package org.eclipse.buildship.ui.util.color;

import com.google.common.base.Preconditions;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

/**
 * Contains helper methods related to colors.
 */
public final class ColorUtils {

    private ColorUtils() {
    }

    /**
     * Retrieves the {@code DECORATIONS_COLOR} from the current workbench theme.
     *
     * @return the theme color to decorate text
     */
    public static Color getDecorationsColorFromCurrentTheme() {
        ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        return Preconditions.checkNotNull(theme.getColorRegistry().get("DECORATIONS_COLOR"));
    }

}
