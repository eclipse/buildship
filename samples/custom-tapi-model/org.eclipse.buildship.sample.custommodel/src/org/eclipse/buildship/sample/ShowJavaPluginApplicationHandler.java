package org.eclipse.buildship.sample;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

public class ShowJavaPluginApplicationHandler extends AbstractHandler {

    @Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
        CustomModelJob job = new CustomModelJob();
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(IJobChangeEvent ignore) {
                new UIJob("") {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        try {
                            displayMessage();
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        return Status.OK_STATUS;
                    }

                    private void displayMessage() throws ExecutionException {
                        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
                        MessageDialog.openInformation(window.getShell(), "Gradle java plugin application info", job.javaPluginInfo);
                    }
                }.schedule();
            }
        });
        job.schedule();
        return null;
    }

    private class CustomModelJob extends Job {

        private String javaPluginInfo;

        public CustomModelJob() {
            super("Check if project applies the 'java' plugin");
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            this.javaPluginInfo = JavaPluginInfoLoader.loadPluginInfo(monitor);
            return Status.OK_STATUS;
        }
    }
}
