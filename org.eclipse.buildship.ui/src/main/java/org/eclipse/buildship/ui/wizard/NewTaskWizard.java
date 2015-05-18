/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Denis Zygann <d.zygann@web.de> - Bug 465728
 */
package org.eclipse.buildship.ui.wizard;

import org.eclipse.jface.wizard.Wizard;

import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaData;

/**
 * TODO: quickfix den wizard aufploppen lassen und in den editor die funktion
 * erstellen. Wizard to create the desired task type function.
 */
public class NewTaskWizard extends Wizard {

    private GradleTaskMetaData metaData;
    private CreateTaskTypeWizardMain pageMain;
    private CreateTaskTypeWizardProperties pageProperties;

    private TaskCreationModel taskCreationModel;

    /**
     * The constructor sets the {@link TaskTypeMetaData} and the title from the
     * window.
     *
     * @param metaData
     */
    public NewTaskWizard(GradleTaskMetaData metaData) {
        super();
        this.metaData = metaData;
        this.taskCreationModel = new TaskCreationModel();
        setWindowTitle("Create a task type");
    }

    @Override
    public void addPages() {
        pageMain = new CreateTaskTypeWizardMain(metaData.getTaskTypes());
        pageProperties = new CreateTaskTypeWizardProperties();
        addPage(pageMain);
        addPage(pageProperties);
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    public TaskCreationModel getTaskCreationModel() {
        return taskCreationModel;
    }

}