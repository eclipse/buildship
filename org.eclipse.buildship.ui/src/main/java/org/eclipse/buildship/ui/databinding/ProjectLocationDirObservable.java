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

package org.eclipse.buildship.ui.databinding;

import java.io.File;

import org.eclipse.core.databinding.observable.value.AbstractObservableValue;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;

/**
 * This observable observes the project dir of a ProjectImportConfiguration.
 *
 */
public class ProjectLocationDirObservable extends AbstractObservableValue {

    private ProjectImportConfiguration projectImportConfiguration;

    public ProjectLocationDirObservable(ProjectImportConfiguration projectImportConfiguration) {
        this.projectImportConfiguration = projectImportConfiguration;
    }

    @Override
    protected Object doGetValue() {
        return this.projectImportConfiguration.getProjectDir().getValue();
    }

    @Override
    protected void doSetValue(Object value) {
        if (value instanceof File) {
            this.projectImportConfiguration.setProjectDir((File) value);
        }
    }

    @Override
    public Object getValueType() {
        return null;
    }
}
