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

package org.eclipse.buildship.ui.internal.util.widget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;

/**
 * Contains helper methods related to the {@link Button} class.
 */
public final class ButtonUtils {

    private ButtonUtils() {
    }

    /**
     * Sets the selection state of the given button. If the state actually changes,
     * a {@link SWT#Selection} event is fired.
     *
     * @param button the button on which to set the selection state
     * @param selected the new selection state
     */
    public static void setSelectionAndFireEvent(Button button, boolean selected) {
        boolean oldSelection = button.getSelection();
        if (oldSelection != selected) {
            button.setSelection(selected);
            button.notifyListeners(SWT.Selection, new Event());
        }
    }

}
