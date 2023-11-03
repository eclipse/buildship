/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution;

import org.eclipse.buildship.ui.internal.view.RemoveAllPagesAction;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * Removes all {@link org.eclipse.buildship.ui.internal.view.Page} elements from the target {@link org.eclipse.buildship.ui.internal.view.MultiPageView}.
 */
public final class RemoveAllTerminatedExecutionPagesAction extends RemoveAllPagesAction {

    public RemoveAllTerminatedExecutionPagesAction(ExecutionPage page) {
        super(page, ExecutionViewMessages.Action_RemoveAllExecutionPages_Tooltip);

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = ((ExecutionPage) getPage()).getProcessDescription().getJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                enableIfCloseable();
            }
        });
    }

}
