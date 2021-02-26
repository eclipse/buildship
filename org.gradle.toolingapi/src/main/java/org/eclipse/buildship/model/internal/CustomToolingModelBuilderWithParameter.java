/*******************************************************************************
 * Copyright (c) 2022 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.model.internal;

import java.lang.reflect.Field;

import org.gradle.api.Project;
import org.gradle.plugins.ide.internal.tooling.EclipseModelBuilder;
import org.gradle.plugins.ide.internal.tooling.eclipse.DefaultEclipseProject;
import org.gradle.tooling.model.eclipse.EclipseRuntime;
import org.gradle.tooling.provider.model.ParameterizedToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

import org.eclipse.buildship.model.ExtendedEclipseModel;

public class CustomToolingModelBuilderWithParameter extends ExtendedEclipseModelBuilder implements ParameterizedToolingModelBuilder<EclipseRuntime> {

    public CustomToolingModelBuilderWithParameter(ToolingModelBuilderRegistry registry) {
        super(registry);
    }

    @Override
    public boolean canBuild(String modelName) {
        return modelName.equals(ExtendedEclipseModel.class.getName());
    }

    @Override
    public Object buildAll(String modelName, EclipseRuntime eclipseRuntime, Project modelRoot) {
        ToolingModelBuilder modelBuilder = this.registry.getBuilder("org.gradle.tooling.model.eclipse.EclipseProject");

        EclipseModelBuilder eclipseModelBuilder = null;
        try {
            // Gradle 7.x returns LenientModelBuilder which cannot be invoked with a parameter
            Field field = modelBuilder.getClass().getDeclaredField("delegate");
            field.setAccessible(true);
            eclipseModelBuilder = (EclipseModelBuilder) field.get(modelBuilder);
        } catch (Exception e) {
            eclipseModelBuilder = (EclipseModelBuilder) modelBuilder;
        }

        DefaultEclipseProject eclipseProject = (DefaultEclipseProject) eclipseModelBuilder.buildAll("org.gradle.tooling.model.eclipse.EclipseProject", eclipseRuntime, modelRoot);
        return build(eclipseProject, modelRoot);
    }

    @Override
    public Class<EclipseRuntime> getParameterType() {
        return EclipseRuntime.class;
    }
}
