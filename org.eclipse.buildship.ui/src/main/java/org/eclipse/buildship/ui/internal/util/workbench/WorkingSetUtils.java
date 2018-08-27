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

package org.eclipse.buildship.ui.internal.util.workbench;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * Contains helper methods related to the Eclipse working sets.
 */
public final class WorkingSetUtils {

    private WorkingSetUtils() {
    }

    /**
     * Converts the given working set names to {@link org.eclipse.ui.IWorkingSet} instances. Filters
     * out working sets that cannot be found by the {@link IWorkingSetManager}.
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

    /**
     * Converts the given working {@link org.eclipse.ui.IWorkingSet} instances to working set names.
     *
     * @param workingSets the working sets
     * @return the names of the working sets
     */
    public static List<String> toWorkingSetNames(List<IWorkingSet> workingSets) {
        return FluentIterable.from(workingSets).transform(new Function<IWorkingSet, String>() {

            @Override
            public String apply(IWorkingSet workingSet) {
                return workingSet.getName();
            }
        }).toList();
    }

    /**
     * Returns the names of the selected {@link org.eclipse.ui.IWorkingSet} instancesor an empty
     * List, if the selection does not contain any {@link org.eclipse.ui.IWorkingSet}.
     *
     * @param selection the selection
     * @return the names of the selected working sets
     */
    public static List<String> getSelectedWorkingSetNames(IStructuredSelection selection) {
        List<IWorkingSet> selectedWorkingSets = getSelectedWorkingSets(selection);
        return toWorkingSetNames(selectedWorkingSets);
    }

    /**
     * Returns the selected {@link org.eclipse.ui.IWorkingSet} instances or an empty List, if the
     * selection does not contain any {@link org.eclipse.ui.IWorkingSet}.
     *
     * @param selection the selection
     * @return the selected working sets
     */
    public static List<IWorkingSet> getSelectedWorkingSets(IStructuredSelection selection) {
        if (selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
            return ImmutableList.of();
        }

        List<?> elements = selection.toList();
        @SuppressWarnings("unchecked")
        List<IWorkingSet> workingSets = (List<IWorkingSet>) FluentIterable.from(elements).filter(Predicates.instanceOf(IWorkingSet.class)).toList();
        return workingSets;
    }

}
