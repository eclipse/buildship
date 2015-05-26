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
 * Removes all {@link org.eclipse.buildship.ui.view.Page} elements from the target {@link org.eclipse.buildship.ui.view.MultiPageView}.
 */
public final class RemoveAllTerminatedExecutionPagesAction extends Action {

    private final ExecutionPage page;
    private final MultiPageView view;

    public RemoveAllTerminatedExecutionPagesAction(ExecutionPage page, MultiPageView view) {
        this.page = Preconditions.checkNotNull(page);
        this.view = Preconditions.checkNotNull(view);

        setToolTipText(ExecutionsViewMessages.Action_RemoveAllExecutionPages_Tooltip);
        setImageDescriptor(PluginImages.REMOVE_ALL_PAGES.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.REMOVE_ALL_PAGES.withState(ImageState.DISABLED).getImageDescriptor());

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = this.page.getBuildJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                RemoveAllTerminatedExecutionPagesAction.this.setEnabled(true);
            }
        });
        setEnabled(job.getState() == Job.NONE);
    }

    @Override
    public void run() {
        this.view.removeAllPages();
    }

}
