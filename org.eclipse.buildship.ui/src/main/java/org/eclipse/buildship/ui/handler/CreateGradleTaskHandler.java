/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaDataManager;
import org.eclipse.buildship.ui.wizard.task.NewGradleTaskWizard;

/**
 * Handler for creating Gradle tasks.
 *
 */
public class CreateGradleTaskHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        Shell activeShell = HandlerUtil.getActiveShell(event);

        GradleTaskMetaDataManager gradleTaskMetaDataManager = new GradleTaskMetaDataManager();
        NewGradleTaskWizard newGradleTaskWizard = new NewGradleTaskWizard(gradleTaskMetaDataManager.getTaskMetaData());

        WizardDialog wizardDialog = new WizardDialog(activeShell, newGradleTaskWizard);

        int open = wizardDialog.open();
        if (Window.OK == open) {
            System.out.println(newGradleTaskWizard.getTaskCreationModel().getTaskTypeFunction());
        }

        return null;
    }

}
