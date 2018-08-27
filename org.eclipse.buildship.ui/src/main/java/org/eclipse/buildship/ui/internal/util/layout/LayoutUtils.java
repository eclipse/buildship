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

package org.eclipse.buildship.ui.internal.util.layout;

import org.eclipse.swt.layout.GridLayout;

/**
 * Contains helper methods related to UI creation.
 */
public final class LayoutUtils {

    private LayoutUtils() {
    }

    /**
     * Creates a new {@link GridLayout} with the given number of columns.
     *
     * @param numOfColumns the number of columns
     * @return the new instance
     */
    public static GridLayout newGridLayout(int numOfColumns) {
        GridLayout layout = new GridLayout(numOfColumns, false);
        layout.verticalSpacing = 0;
        return layout;
    }

}
