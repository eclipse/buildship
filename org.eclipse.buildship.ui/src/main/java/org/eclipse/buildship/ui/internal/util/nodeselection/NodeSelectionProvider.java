/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
