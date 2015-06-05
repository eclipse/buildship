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

package org.eclipse.buildship.ui.util.workbench;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import java.util.List;

/**
 * Contains helper methods related to the Eclipse working sets.
 */
public final class WorkingSetUtils {

    private WorkingSetUtils() {
    }

    /**
     * Converts the given working set names to {@link org.eclipse.ui.IWorkingSet} instances.
     * Filters out working sets that cannot be found by the {@link IWorkingSetManager}.
     *
     * @param workingSetNames the names of the working sets
     * @return the {@link org.eclipse.ui.IWorkingSet} instances
     */
    public static IWorkingSet[] toWorkingSets(List<String> workingSetNames) {
        final IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
        return FluentIterable.from(workingSetNames).transform(new Function<String, IWorkingSet>() {
            @Override
            public IWorkingSet apply(String name) {
                return workingSetManager.getWorkingSet(name);
            }
        }).filter(Predicates.notNull()).toArray(IWorkingSet.class);
    }

}
