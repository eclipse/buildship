/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

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
