/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.view.execution;

import org.gradle.tooling.events.test.Destination;
import org.gradle.tooling.events.test.TestOutputDescriptor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Sorts the test output mesages in the Executions view.
 * <p>
 * The messages printed on standard error will come after the ones on standard out.
 */
public class ExecutionPageSorter extends ViewerComparator {

    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        //
        if (e1 instanceof OperationItem && e2 instanceof OperationItem) {
            OperationItem i1 = (OperationItem) e1;
            OperationItem i2 = (OperationItem) e2;
            if (i1.getDescriptor() instanceof TestOutputDescriptor && i2.getDescriptor() instanceof TestOutputDescriptor) {
                Destination d1 = ((TestOutputDescriptor) i1.getDescriptor()).getDestination();
                Destination d2 = ((TestOutputDescriptor) i2.getDescriptor()).getDestination();
                if (d1 == Destination.StdOut && d2 == Destination.StdErr) {
                    return -1;
                } else if (d1 == Destination.StdErr && d2 == Destination.StdOut) {
                    return 1;
                } else {
                    return 0;
                }

            }
        }
        return super.compare(viewer, e1, e2);
    }
}
