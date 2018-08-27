package org.eclipse.buildship.ui.internal.view.execution

import org.gradle.tooling.LongRunningOperation
import org.gradle.tooling.events.OperationDescriptor
import org.gradle.tooling.events.ProgressEvent
import org.gradle.tooling.events.StartEvent

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.IStatus
import org.eclipse.core.runtime.Status
import org.eclipse.core.runtime.jobs.Job
import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunchConfiguration
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.PlatformUI

import org.eclipse.buildship.core.internal.console.ProcessDescription
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.internal.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.ui.internal.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.ui.internal.util.workbench.WorkbenchUtils
import org.eclipse.buildship.ui.internal.view.execution.ExecutionsView

class ExecutionsViewPerformanceTest extends ProjectSynchronizationSpecification {

    ExecutionsView view

    def setup() {
        runOnUiThread {
            view = WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_ACTIVATE)
        }
    }

    def "Executions view can efficiently present large number of items"() {
            setup:
            LongRunningOperation operation = Mock(LongRunningOperation)

            when:
            runOnUiThread { view.addExecutionPage(processDescription(), operation) }
            long start = System.currentTimeMillis()
            1000.times {  view.pages[0].progressListener.statusChanged(progressEvent()) }
            runOnUiThread { }
            long end = System.currentTimeMillis()

            then:
            end - start < 10000
            view.pages.size() == 1

            cleanup:
            runOnUiThread { view.removeAllPages() }
        }

    private ProcessDescription processDescription() {
        Stub(ProcessDescription) {
            getName() >> 'test-process-description-name'
            getConfigurationAttributes() >> GradleRunConfigurationAttributes.from(createGradleLaunchConfig())
            getJob() >> new EmptyJob()
            isRerunnable() >> false
        }
    }

    private ProgressEvent progressEvent() {
        ProgressEvent event = Mock(StartEvent)
        event.displayName >> 'progress event display name'
        event.eventTime >> System.currentTimeMillis()
        event.descriptor >> descriptor()
        event
    }

    private OperationDescriptor descriptor() {
        OperationDescriptor descriptor = Mock(OperationDescriptor)
        descriptor.name >> 'operation name'
        descriptor.displayName >> 'operation display name'
        descriptor
    }

    class EmptyJob extends Job {

        EmptyJob() {
            super('test-job')
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            Status.OK_STATUS
        }
    }
}
