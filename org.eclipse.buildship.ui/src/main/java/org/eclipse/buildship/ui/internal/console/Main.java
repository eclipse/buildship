package org.eclipse.buildship.ui.internal.console;

import org.eclipse.swt.*;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class Main {

    public static void main(String[] args) {
        Display display = new Display();
        Shell shell = new Shell(display);
        shell.setText("Snippet 356");
        FillLayout layout = new FillLayout();
        layout.marginHeight = layout.marginWidth = 10;
        shell.setLayout(layout);

        String string = "This is sample text with a link and some other link here.";
        final StyledText styledText = new StyledText(shell, SWT.MULTI | SWT.BORDER);
        styledText.setText(string);

        String link1 = "link";
        String link2 = "here";
        StyleRange style = new StyleRange();
        style.underline = true;
        style.underlineStyle = SWT.UNDERLINE_LINK;

        int[] ranges = { string.indexOf(link1), link1.length(), string.indexOf(link2), link2.length() };
        StyleRange[] styles = { style, style };
        styledText.setStyleRanges(ranges, styles);

        styledText.addListener(SWT.MouseDown, event -> {
            // It is up to the application to determine when and how a link should be activated.
            // In this snippet links are activated on mouse down when the control key is held down
            if ((event.stateMask & SWT.MOD1) != 0) {
                int offset = styledText.getOffsetAtPoint(new Point(event.x, event.y));
                if (offset != -1) {
                    StyleRange style1 = null;
                    try {
                        style1 = styledText.getStyleRangeAtOffset(offset);
                    } catch (IllegalArgumentException e) {
                        // no character under event.x, event.y
                    }
                    if (style1 != null && style1.underline && style1.underlineStyle == SWT.UNDERLINE_LINK) {
                        System.out.println("Click on a Link");
                    }
                }
            }
        });
        shell.setSize(600, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }
}
