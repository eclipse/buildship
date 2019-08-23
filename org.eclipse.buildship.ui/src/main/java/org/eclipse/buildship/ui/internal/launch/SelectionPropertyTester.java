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

package org.eclipse.buildship.ui.internal.launch;

import java.util.Collection;

import org.eclipse.core.expressions.PropertyTester;

import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.launch.JavaElementSelection;
import org.eclipse.buildship.core.internal.launch.TestExecutionTarget;

/**
 * Property tester to determine if the test launch shortcut should be visible in the context menus.
 */
public final class SelectionPropertyTester extends PropertyTester {

    private static final String PROPERTY_NAME_SELECTION_CAN_EXECUTE_TEST_RUN = "selectioncanbelaunchedastest";
    private static final String PROPERTY_NAME_SELECTION_CAN_EXECUTE_TEST_DEBUG = "selectioncanbelaunchedastestdebug";

    @Override
    public boolean test(Object receiver, String propertyString, Object[] args, Object expectedValue) {
        if (propertyString.equals(PROPERTY_NAME_SELECTION_CAN_EXECUTE_TEST_RUN)) {
            return receiver instanceof Collection && selectionIsLaunchableAsTest((Collection<?>) receiver);
        } else if (propertyString.equals(PROPERTY_NAME_SELECTION_CAN_EXECUTE_TEST_DEBUG)) {
            return receiver instanceof Collection && selectionIsLaunchableAsTestDebug((Collection<?>) receiver);
        } else {
            throw new GradlePluginsRuntimeException("Unrecognized test property: " + propertyString);
        }
    }

    private boolean selectionIsLaunchableAsTest(Collection<?> elements) {
        JavaElementSelection selection = SelectionJavaElementResolver.from(elements);
        return !TestExecutionTarget.from(selection, "run").validate().isPresent();

    }

    private boolean selectionIsLaunchableAsTestDebug(Collection<?> elements) {
        JavaElementSelection selection = SelectionJavaElementResolver.from(elements);
        return !TestExecutionTarget.from(selection, "debug").validate().isPresent();
    }
}
