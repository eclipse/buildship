/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.composite;

import org.eclipse.ui.IWorkingSet;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.IGradleCompositeIDs;

/**
 * Property tester to determine if the test launch shortcut should be visible in the context menus.
 */
public final class WorkingSetProperyTester extends org.eclipse.core.expressions.PropertyTester {

    private static final String PROPERTY_NAME_IS_GRADLE_COMPOSITE_ID = "gradlecomposite";
    private static final String GRADLE_COMPOSITE_ID = IGradleCompositeIDs.NATURE;

    @Override
    public boolean test(Object receiver, String propertyString, Object[] args, Object expectedValue) {
        if (propertyString.equals(PROPERTY_NAME_IS_GRADLE_COMPOSITE_ID)) {
            return receiver instanceof IWorkingSet && isGradleComposite((IWorkingSet)receiver);
        } else {
            throw new GradlePluginsRuntimeException("Unrecognized property to test: " + propertyString);
        }
    }

    private boolean isGradleComposite(IWorkingSet workingSet) {
        try {
            return workingSet.getId().equals(GRADLE_COMPOSITE_ID);
        } catch (Exception e) {
            return false;
        }
    }

}

