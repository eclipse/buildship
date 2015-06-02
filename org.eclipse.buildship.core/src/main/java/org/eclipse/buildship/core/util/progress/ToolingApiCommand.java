package org.eclipse.buildship.core.util.progress;

/**
 * A command that runs a Tooling API operation.
 */
public interface ToolingApiCommand {

    /**
     * Runs a Tooling API operation.
     *
     * @throws Exception thrown if running the operation fails
     */
    void run() throws Exception;

}
