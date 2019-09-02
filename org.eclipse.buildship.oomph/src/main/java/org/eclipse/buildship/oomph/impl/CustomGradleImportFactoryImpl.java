/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.oomph.impl;

import org.eclipse.buildship.oomph.GradleImportTask;

public class CustomGradleImportFactoryImpl extends GradleImportFactoryImpl {

    @Override
    public GradleImportTask createGradleImportTask() {
        return new CustomGradleImportTaskImpl();
    }
}
