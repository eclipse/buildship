package org.eclipse.buildship.ui.wizard.project;

/**
 * Implemented by components for which there is a help context id.
 */
public interface HelpContextIdProvider {

    /**
     * Returns the help context id.
     *
     * @return the help context id
     */
    String getHelpContextId();

}
