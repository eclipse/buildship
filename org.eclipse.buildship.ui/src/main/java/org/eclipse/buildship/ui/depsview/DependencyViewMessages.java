package org.eclipse.buildship.ui.depsview;

import org.eclipse.osgi.util.NLS;

/**
 * Lists the i18n resource keys for the dependencies view messages.
 */
public class DependencyViewMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.depsview.DependencyViewMessages"; //$NON-NLS-1$

    public static String Label_Reload_Problem;
    public static String Label_No_Gradle_Projects;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, DependencyViewMessages.class);
    }

    private DependencyViewMessages() {
    }
}
