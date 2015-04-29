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

import java.util.Map;

import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaData;
import org.eclipse.buildship.core.model.taskmetadata.TaskType;
import org.eclipse.jface.wizard.Wizard;

/**
 * TODO: quickfix den wizard aufploppen lassen und in den editor die funktion
 * erstellen. Wizard to create the desired task type function.
 */
public class ExampleTaskTypeWizard extends Wizard {

    private static final String TYPE = "type:";

    private static String METHOD_TYPE = "task";

    private GradleTaskMetaData metaData;
    private CreateTaskTypeWizardPageOne pageOne;
    private CreateTaskTypeWizardPageTwo pageTwo;

    /**
     * The constructor sets the {@link TaskTypeMetaData} and the title from the
     * window.
     * 
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

    /**
     * Creates the task type function.
     * 
     * @return
     */
    public String getTaskTypeFunction() {

        TaskType selectedTaskType = pageOne.getSelectedTaskType();
        String taskName = pageOne.getTaskName();
        Map<String, String> filledProperties = pageTwo.getFilledProperties();

        StringBuilder sb = new StringBuilder();
        sb.append(METHOD_TYPE);
        sb.append(" ");
        sb.append(taskName);
        sb.append("(");
        sb.append(TYPE);
        sb.append(" ");
        sb.append(selectedTaskType.getClassName());
        sb.append(") {");
        sb.append(System.lineSeparator());
        for (Map.Entry<String, String> properties : filledProperties.entrySet()) {
            sb.append(properties.getKey());
            sb.append(" ");
            sb.append(properties.getValue());
            sb.append(System.lineSeparator());
        }
        sb.append("}");

        return sb.toString();
    }
}
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