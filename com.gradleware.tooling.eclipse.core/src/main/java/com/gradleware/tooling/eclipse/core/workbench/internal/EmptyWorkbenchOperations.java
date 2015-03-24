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

package com.gradleware.tooling.eclipse.core.workbench.internal;

import com.gradleware.tooling.eclipse.core.workbench.WorkbenchOperations;

/**
 * Empty implementation of the {@link WorkbenchOperations} interface since the core plugin does
 * not know about the workbench which is a part of the UI.
 */
public final class EmptyWorkbenchOperations implements WorkbenchOperations {

    @Override
    public void activateTestRunnerView() {
        // do nothing
    }

}
