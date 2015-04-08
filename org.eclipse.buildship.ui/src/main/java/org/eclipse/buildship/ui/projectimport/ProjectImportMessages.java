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

package org.eclipse.buildship.ui.projectimport;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the project import messages.
 */
public final class ProjectImportMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.projectimport.ProjectImportMessages"; //$NON-NLS-1$

    public static String Title_GradleProjectWizardPage;
    public static String Title_GradleOptionsWizardPage;
    public static String Title_PreviewImportWizardPage;

    public static String Title_Select_0;

    public static String Button_Label_Browse;

    public static String Label_ProjectRootDirectory;
    public static String Label_GradleUserHome;
    public static String Label_JavaHome;
    public static String Label_JvmArguments;
    public static String Label_ProgramArguments;

    public static String Label_AdvancedOptions;
    public static String Label_GradleDistribution;
    public static String Label_GradleVersion;
    public static String Label_ProjectStructure;

    public static String InfoMessage_GradleProjectWizardPageDefault;
    public static String InfoMessage_GradleOptionsWizardPageDefault;
    public static String InfoMessage_PreviewImportWizardPageDefault;

    public static String InfoMessage_GradleProjectWizardPageContext;
    public static String InfoMessage_GradleDistributionWizardPageContext;
    public static String InfoMessage_AdvancedOptionsWizardPageContext;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ProjectImportMessages.class);
    }

    private ProjectImportMessages() {
    }

}
