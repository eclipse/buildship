/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.core.internal.workspace;

import java.io.File;

import org.eclipse.buildship.core.internal.CorePlugin;

public class BuildComposite {

    public static File preferencesFile(String projectName) {
        File preferencesFile = CorePlugin.getInstance().getStateLocation().append("workspace-composites").append(projectName).toFile();
        if (preferencesFile.canWrite()) {
            return preferencesFile;
        } else {
            preferencesFile.getParentFile().mkdir();
            return preferencesFile;
        }
    }
}
