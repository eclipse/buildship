/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.gradle;

import com.google.common.collect.ImmutableList;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import java.util.List;

/**
 * Contains helper methods related to Gradle.
 */
public final class GradleUtils {

    private GradleUtils() {
    }

    /**
     * Filters away those tests that are a child of a test from the given list of tests.
     *
     * @param testDescriptors the tests to filter
     * @return the filtered tests where no test has as a parent a test that is also part of the result
     */
    public static List<TestOperationDescriptor> filterChildren(List<TestOperationDescriptor> testDescriptors) {
        ImmutableList.Builder<TestOperationDescriptor> withoutChildren = ImmutableList.builder();
        for (TestOperationDescriptor testDescriptor : testDescriptors) {
            if (!isParentSelected(testDescriptor, testDescriptors)) {
                withoutChildren.add(testDescriptor);
            }
        }
        return withoutChildren.build();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private static boolean isParentSelected(TestOperationDescriptor candidate, List<TestOperationDescriptor> selectedTestDescriptors) {
        OperationDescriptor parent = candidate.getParent();
        if (parent instanceof TestOperationDescriptor) {
            return selectedTestDescriptors.contains(parent) || isParentSelected((TestOperationDescriptor) parent, selectedTestDescriptors);
        } else {
            return false;
        }
    }

}
