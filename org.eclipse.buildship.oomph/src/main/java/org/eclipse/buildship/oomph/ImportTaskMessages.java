/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
