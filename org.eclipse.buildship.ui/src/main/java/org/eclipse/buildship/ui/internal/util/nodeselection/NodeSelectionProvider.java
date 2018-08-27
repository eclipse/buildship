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

package org.eclipse.buildship.ui.internal.util.nodeselection;

/**
 * Provides the current selection of a visual component, respecting the temporal order of the selection.
 */
public interface NodeSelectionProvider {

    /**
     * Returns the current selection.
     *
     * @return the current selection
     */
    NodeSelection getSelection();

}
