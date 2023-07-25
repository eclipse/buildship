/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.internal.test.fixtures;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Since older versions of Eclipse are compiled such that they are not compatible with Spock, we
 * have to obtain certain references indirectly via this class.
 * <p/>
 * The abstract modifier is set in order to be excluded from the test execution.
 */
public abstract class LegacyEclipseSpockTestHelper {

    private LegacyEclipseSpockTestHelper() {
    }

    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

}
