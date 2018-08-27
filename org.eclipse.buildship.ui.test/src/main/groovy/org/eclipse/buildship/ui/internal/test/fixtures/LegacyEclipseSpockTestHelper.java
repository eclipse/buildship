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

package org.eclipse.buildship.ui.internal.test.fixtures;

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
