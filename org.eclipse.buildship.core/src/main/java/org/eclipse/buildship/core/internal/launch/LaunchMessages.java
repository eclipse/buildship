/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.launch;

import org.eclipse.osgi.util.NLS;

public class LaunchMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.core.internal.launch.LaunchMessages"; //$NON-NLS-1$
    public static String Validation_Message_NoTests_0;
    public static String Validation_Message_NoProject;
    public static String Validation_Message_DifferentProject;
    public static String Validation_Message_NullProject;
    public static String Validation_Message_ClosedProject_0;
    public static String Validation_Message_NotGradleProject_0;
    public static String Validation_Message_BinaryType;
    public static String Validation_Message_NotTestType;
    public static String Validation_Message_BinaryMethod;
    public static String Validation_Message_NoTestDebugSupport_0_1;
    public static String Validation_Message_NoTests;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, LaunchMessages.class);
    }

    private LaunchMessages() {
    }
}
