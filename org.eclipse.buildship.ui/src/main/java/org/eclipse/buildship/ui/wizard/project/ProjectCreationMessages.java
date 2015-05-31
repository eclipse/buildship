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

package org.eclipse.buildship.ui.wizard.project;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the project creation messages.
 */
public final class ProjectCreationMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.wizard.project.ProjectCreationMessages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ProjectCreationMessages.class);
    }

    public static String Title_NewGradleProjectWizardPage;
    public static String Title_NewGradleProjectPreviewWizardPage;

    public static String InfoMessage_NewGradleProjectWizardPageDefault;
    public static String InfoMessage_NewGradleProjectPreviewWizardPageDefault;

    public static String InfoMessage_NewGradleProjectWizardPageContext;

    public static String Label_ProjectName;
    public static String Label_ProjectLocation;
    public static String Label_CustomLocation;
    public static String Label_WorkingSets;
    public static String Button_UseDefaultLocation;

    private ProjectCreationMessages() {
    }

}
