/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Denis Zygann <d.zygann@web.de> - Bug 465728
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.wizard.task;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaData;

/**
 * This wizard is used to create new Gradle Tasks.
 */
public class NewGradleTaskWizard extends Wizard {

    private GradleTaskMetaData metaData;
    private CreateTaskTypeWizardMainPage pageMain;
    private CreateTaskTypeWizardPropertiesPage pageProperties;

    private TaskCreationModel taskCreationModel;

    /**
     * The constructor sets the {@link TaskTypeMetaData} and the title from the
     * window.
     *
     * @param metaData
     */
    public NewGradleTaskWizard(GradleTaskMetaData metaData) {
        super();
        this.metaData = metaData;
        this.taskCreationModel = new TaskCreationModel();
        setWindowTitle("Create a Gradle Task");
    }

    @Override
    public void addPages() {
        pageMain = new CreateTaskTypeWizardMainPage(metaData.getTaskTypes(), this.taskCreationModel);
        pageProperties = new CreateTaskTypeWizardPropertiesPage(this.taskCreationModel);
        addPage(pageMain);
        addPage(pageProperties);

        if (getContainer() instanceof IPageChangeProvider) {
            IPageChangeProvider pageChangeProvider = (IPageChangeProvider) getContainer();
            pageChangeProvider.addPageChangedListener(new IPageChangedListener() {

                @Override
                public void pageChanged(PageChangedEvent event) {
                    Object selectedPage = event.getSelectedPage();
                    if (pageProperties.equals(selectedPage)) {
                        // clear the old property values, which may have been added
                        taskCreationModel.getTaskPropertyValues().clear();
                        pageProperties.showTaskTypeProperties();
                    }
                }
            });
        }
    }

    @Override
    public boolean performFinish() {
        return true;
    }

    public TaskCreationModel getTaskCreationModel() {
        return taskCreationModel;
    }

}