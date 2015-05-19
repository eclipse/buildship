package org.eclipse.buildship.core.util.progress

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.buildship.core.Logger;
import org.eclipse.buildship.core.notification.UserNotification;
import org.eclipse.buildship.core.test.fixtures.TestEnvironment;

import spock.lang.Specification

class ToolingApiWorkspaceJobTest extends Specification {

    def setup () {
        // suppress the error messages from the console
        TestEnvironment.registerService(Logger, Mock(Logger))
        TestEnvironment.registerService(UserNotification, Mock(UserNotification))
    }

    def cleanup() {
        TestEnvironment.cleanup()
    }

    def "Absent root cause should not throw NPE"() {
        setup:
        def job = new ToolingApiWorkspaceJob("Test") {
                    @Override
                    protected void runToolingApiJobInWorkspace(IProgressMonitor monitor) throws Exception {
                        throw new UnsupportedOperationException()
                    };
                }

        when:
        job.schedule()
        job.join();

        then:
        job.result.severity == IStatus.INFO
        job.result.exception instanceof UnsupportedOperationException
    }

}
