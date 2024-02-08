/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

import java.util.List;

import org.eclipse.ui.IWorkingSet;

/**
 * A {@link WorkingSetChangedListener} gets informed when the selected {@link IWorkingSet} instances
 * of the {@link WorkingSetConfigurationWidget} change.
 *
 * @see WorkingSetConfigurationWidget
 */
public interface WorkingSetChangedListener {

    /**
     * Invoked when the working sets change.
     *
     * @param workingSets the new working sets
     */
    void workingSetsChanged(List<IWorkingSet> workingSets);

}
