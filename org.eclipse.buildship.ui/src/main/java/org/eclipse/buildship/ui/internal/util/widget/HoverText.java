/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.util.widget;

import com.google.common.base.Preconditions;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

/**
 * Displays a hover message for the target UI control.
 * <p/>
 * The implementation is based on the HoverHelp example found in the <a href=
 * "https://github.com/eclipse/eclipse.platform.swt/tree/master/examples/org.eclipse.swt.examples/src/org/eclipse/swt/examples/hoverhelp">eclipse/eclipse.plaform.swt</a>
 * repository.
 *
 * @author Donat Csikos
 */
public final class HoverText {

    private final Control target;
    private final String text;

    private AbstractInformationControl hover;

    private HoverText(Control target, String text) {
        this.target = Preconditions.checkNotNull(target);
        this.text =  Preconditions.checkNotNull(text);
    }

    public static HoverText createAndAttach(Control target, String text) {
        HoverText hoverHelp = new HoverText(target, text);
        hoverHelp.createControl(target.getShell());
        return hoverHelp;
    }

    private void createControl(Shell parent) {
        this.hover = new DefaultInformationControl(parent, "Gradle Info");
        this.hover.setSizeConstraints(300, 300);
        this.hover.setInformation(this.text);
        Point size = this.hover.computeSizeHint();
        this.hover.setSize(size.x, size.y);
        addListeners();
    }

    private void addListeners() {
        MouseHoverEventListener listener = new MouseHoverEventListener();
        this.target.addMouseListener(listener);
        this.target.addMouseMoveListener(listener);
        this.target.addMouseTrackListener(listener);
    }

    /**
     * Updates tooltip based on mouse events.
     */
    private class MouseHoverEventListener extends MouseTrackAdapter implements MouseListener, MouseMoveListener {

        private Widget currentWidget;

        @Override
        public void mouseMove(MouseEvent event) {
            Point point = new Point(event.x, event.y);
            Widget widget = event.widget;
            if (widget == null) {
               HoverText.this.hover.setVisible(false);
                this.currentWidget = null;
                return;
            }
            if (widget == this.currentWidget) {
                return;
            }
            this.currentWidget = widget;
            HoverText.this.hover.setInformation(HoverText.this.text);
            Point currentPosition = HoverText.this.target.toDisplay(point);
            setHoverLocation(currentPosition);
            HoverText.this.hover.setVisible(true);
        }

        private void setHoverLocation(Point position) {
            Rectangle displayBounds = HoverText.this.target.getDisplay().getBounds();
            Rectangle shellBounds = HoverText.this.hover.getBounds();
            int x = Math.max(Math.min(position.x, displayBounds.width - shellBounds.width), 0);
            int y = Math.max(Math.min(position.y + 16, displayBounds.height - shellBounds.height), 0);
            HoverText.this.hover.setLocation(new Point(x, y));
        }

        @Override
        public void mouseExit(MouseEvent event) {
            if (HoverText.this.hover.isVisible()) {
                HoverText.this.hover.setVisible(false);
            }
            this.currentWidget = null;
        }

        @Override
        public void mouseDown(MouseEvent event) {
            // remote tooltip if the users clicks on the control
            if (HoverText.this.hover.isVisible()) {
                HoverText.this.hover.setVisible(false);
            }
        }

        @Override
        public void mouseDoubleClick(MouseEvent event) {
        }

        @Override
        public void mouseUp(MouseEvent e) {
        }
    }
}
