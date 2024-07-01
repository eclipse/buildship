/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

public class ExternalGradleProject {
    //TODO (kuzniarz) initial implementation needs to be finished

    private String projectName;
    private String projectPath;

    public ExternalGradleProject(String name, String path) {
        this.projectName = name;
        this.projectPath = path;
    }

    public String getProjectName() {
        return this.projectName;
    }

    public String getProjectPath() {
        return this.projectPath;
    }
}
