/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseProjectDependency;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.omnimodel.OmniAccessRule;
import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;
import org.eclipse.buildship.core.omnimodel.OmniEclipseProjectDependency;

/**
 * Default implementation of the {@link OmniEclipseProjectDependency} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseProjectDependency extends AbstractOmniClasspathEntry implements OmniEclipseProjectDependency {

    private final String path;
    private final boolean exported;

    private DefaultOmniEclipseProjectDependency(String path, boolean exported, Optional<List<OmniClasspathAttribute>> attributes, Optional<List<OmniAccessRule>> accessRules) {
        super(attributes, accessRules);
        this.path = path;
        this.exported = exported;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public boolean isExported() {
        return this.exported;
    }

    public static DefaultOmniEclipseProjectDependency from(EclipseProjectDependency projectDependency) {
        return new DefaultOmniEclipseProjectDependency(
                projectDependency.getPath(),
                getIsExported(projectDependency),
                getClasspathAttributes(projectDependency),
                getAccessRules(projectDependency));
    }

    /**
     * EclipseProjectDependency#isExported is only available in Gradle versions >= 2.5.
     *
     * @param projectDependency the project dependency model
     */
    private static boolean getIsExported(EclipseProjectDependency projectDependency) {
        try {
            return projectDependency.isExported();
        } catch (Exception ignore) {
            return true;
        }
    }

}
