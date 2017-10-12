package org.eclipse.buildship.ui.view.execution

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

import org.eclipse.buildship.core.console.ProcessDescription
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.ui.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils
import org.eclipse.buildship.ui.view.execution.ExecutionsView

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
            end - start < 5000
            view.pages.size() == 1

            cleanup:
            runOnUiThread { view.removeAllPages() }
        }

    private ProcessDescription processDescription() {
        Stub(ProcessDescription) {
            getName() >> 'test-process-description-name'
            getConfigurationAttributes() >> GradleRunConfigurationAttributes.from(emptyLaunchConfiguration())
            getJob() >> new EmptyJob()
            isRerunnable() >> false
        }
    }

    private ILaunchConfiguration emptyLaunchConfiguration() {
        ILaunchManager launchManager = DebugPlugin.default.launchManager
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID)
        type.newInstance(null, "launch-config-name")
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
