package org.eclipse.buildship.ui.part.execution;

import org.eclipse.osgi.util.NLS;

public class ExecutionViewMessages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.buildship.ui.part.execution.ExecutionViewMessages"; //$NON-NLS-1$

    public static String Label_No_Execution;

    public static String Tree_Column_Duration_Text;
    public static String Tree_Column_Operation_Text;

    public static String Tree_Item_Root_Text;
    public static String Tree_Item_Tests_Text;
    public static String Tree_Item_Test_Finished_Text;
    public static String Tree_Item_Test_Started_Text;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, ExecutionViewMessages.class);
    }

    private ExecutionViewMessages() {
    }
}
