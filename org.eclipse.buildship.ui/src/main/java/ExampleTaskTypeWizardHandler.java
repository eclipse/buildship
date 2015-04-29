
/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Denis Zygann <d.zygann@web.de>  - Bug 465728 
 */
import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaData;
import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaDataManager;
import org.eclipse.buildship.ui.wizard.ExampleTaskTypeWizard;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler to execute the {@link ExampleTaskTypeWizard}.
 */
public class ExampleTaskTypeWizardHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        GradleTaskMetaDataManager dataManager = new GradleTaskMetaDataManager();
        GradleTaskMetaData metaData = dataManager.getTaskMetaData();
        Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();

        ExampleTaskTypeWizard wizard = new ExampleTaskTypeWizard(metaData);
        WizardDialog wizardDialog = new WizardDialog(shell, wizard);
        if (wizardDialog.open() == Window.OK) {
            System.out.println(wizard.getTaskTypeFunction());
        }

        return null;
    }

}