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


import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaData;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard to create the desired task type function.
 */
public class ExampleTaskTypeWizard extends Wizard {

    private GradleTaskMetaData metaData;
    private CreateTaskTypeWizardPageOne pageOne;
    private CreateTaskTypeWizardPageTwo pageTwo;

    /**
     * The constructor sets the {@link TaskTypeMetaData} and the title from the window.
     * @param metaData
     */
    public ExampleTaskTypeWizard(GradleTaskMetaData metaData) {
        super();
        this.metaData = metaData;
        setWindowTitle("Create a task type");
    }

    @Override
    public void addPages() {
        pageOne = new CreateTaskTypeWizardPageOne(metaData.getTaskTypes());
        pageTwo = new CreateTaskTypeWizardPageTwo();
        addPage(pageOne);
        addPage(pageTwo);
    }

    @Override
    public boolean performFinish() {
        return true;
    }
}