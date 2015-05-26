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

package org.eclipse.buildship.ui.view.execution;

import com.google.common.base.Preconditions;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.view.MultiPageView;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;

/**
 * Removes the target {@link org.eclipse.buildship.ui.view.execution.ExecutionPage} from the
 * {@link org.eclipse.buildship.ui.view.MultiPageView} to which this page belongs.
 */
public final class RemoveTerminatedExecutionPageAction extends Action {

    private final ExecutionPage page;

    public RemoveTerminatedExecutionPageAction(ExecutionPage page) {
        this.page = Preconditions.checkNotNull(page);

        setToolTipText(ExecutionsViewMessages.Action_RemoveExecutionPage_Tooltip);
        setImageDescriptor(PluginImages.REMOVE_PAGE.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_PAGE.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getBuildJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                RemoveTerminatedExecutionPageAction.this.setEnabled(true);
            }
        });
        setEnabled(job.getState() == Job.NONE);
    }

    @Override
    public void run() {
        MultiPageView view = (MultiPageView) this.page.getSite().getViewSite().getPart();
        view.removePage(this.page);
    }

}
