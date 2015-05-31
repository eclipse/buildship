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

package org.eclipse.buildship.ui.wizard.project;

import com.google.common.base.Preconditions;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;

import java.io.File;

/**
 * Observes the project directory of a {@code ProjectImportConfiguration} instance.
 */
public final class ProjectImportConfigurationProjectDirObservable extends AbstractObservableValue {

    private final ProjectImportConfiguration configuration;

    public ProjectImportConfigurationProjectDirObservable(ProjectImportConfiguration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
    }

    @Override
    protected Object doGetValue() {
        return this.configuration.getProjectDir().getValue();
    }

    @Override
    protected void doSetValue(Object value) {
        this.configuration.setProjectDir((File) value);
    }

    @Override
    public Object getValueType() {
        return File.class;
    }

}
