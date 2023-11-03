/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph;

import org.eclipse.osgi.util.NLS;

public class ImportTaskMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.oomph.messages"; //$NON-NLS-1$
    public static String GradleImportTaskImpl_found_existing;
    public static String GradleImportTaskImpl_import_new;
    public static String GradleImportTaskImpl_importing;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ImportTaskMessages.class);
    }

    private ImportTaskMessages() {
    }
}
