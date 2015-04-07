/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild.updatesite

import org.gradle.api.Project


/**
 * Fields to set for the {@link SignBundlesTask} task.
 */
class SignBundlesConvention {

    final Project project

    public SignBundlesConvention(Project project) {
        this.project = project
    }

    def getSignBundles() {
        project.updateSite.signBundles
    }
}
