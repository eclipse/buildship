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

import org.eclipse.buildship.ui.view.RemovePageAction;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * Removes the target {@link org.eclipse.buildship.ui.view.execution.ExecutionPage} from the
 * {@link org.eclipse.buildship.ui.view.MultiPageView} to which this page belongs.
 */
public final class RemoveTerminatedExecutionPageAction extends RemovePageAction {

    public RemoveTerminatedExecutionPageAction(ExecutionPage page) {
        super(page, ExecutionsViewMessages.Action_RemoveExecutionPage_Tooltip);

        registerJobChangeListener();
    }

    private void registerJobChangeListener() {
        Job job = ((ExecutionPage) getPage()).getBuildJob();
        job.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                enableIfCloseable();
            }
        });
        enableIfCloseable();
    }

    private void enableIfCloseable() {
        setEnabled(getPage().isCloseable());
    }

}
