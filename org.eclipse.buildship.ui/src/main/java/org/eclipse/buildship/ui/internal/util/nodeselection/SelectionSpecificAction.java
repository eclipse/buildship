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

import org.eclipse.jface.action.IAction;

/**
 * Describes an {@link IAction} instance that knows when it can be <i>shown</i> and when it can be
 * <i>enabled</i> for a given selection state.
 */
public interface SelectionSpecificAction extends IAction {

    /**
     * Returns {@code true} if this action should be shown for the given selection.
     *
     * @param selection the selection from which to make the decision
     * @return {@code true} if this action should be shown
     */
    boolean isVisibleFor(NodeSelection selection);

    /**
     * Returns {@code true} if this action should be enabled for the given selection.
     *
     * @param selection the selection from which to make the decision
     * @return {@code true} if this action should be enabled
     */
    boolean isEnabledFor(NodeSelection selection);

    /**
     * Sets the enabled state of this action depending on the given selection.
     *
     * @param selection the selection from which to make the decision
     */
    void setEnabledFor(NodeSelection selection);

}
