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

package com.gradleware.tooling.eclipse.ui;

/**
 * Help context registered via the <code>org.eclipse.help.contexts</code> extension point in the
 * plugin.xml.
 */
public final class HelpContext {

    // the help context ID has to be in the following format: ${PLUGIN_ID}.${CONTEXT_ID}
    // the context id is defined in the external (xml) file specified in the plugin.xml
    public static final String PROJECT_IMPORT = UiPlugin.PLUGIN_ID + ".projectimport"; //$NON-NLS-1$

    private HelpContext() {
    }

}
