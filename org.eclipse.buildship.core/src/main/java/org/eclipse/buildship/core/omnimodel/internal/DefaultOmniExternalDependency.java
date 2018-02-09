/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.io.File;
import java.util.List;

import org.gradle.tooling.model.ExternalDependency;
import org.gradle.tooling.model.eclipse.EclipseExternalDependency;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.omnimodel.OmniAccessRule;
import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;
import org.eclipse.buildship.core.omnimodel.OmniExternalDependency;

/**
 * Default implementation of the {@link OmniExternalDependency} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniExternalDependency extends AbstractOmniClasspathEntry implements OmniExternalDependency {

    private final File file;
    private final File source;
    private final File javadoc;
    private final boolean exported;

    private DefaultOmniExternalDependency(File file, File source, File javadoc, boolean exported, Optional<List<OmniClasspathAttribute>> attributes, Optional<List<OmniAccessRule>> accessRules) {
        super(attributes, accessRules);
        this.file = file;
        this.source = source;
        this.javadoc = javadoc;
        this.exported = exported;
    }

    @Override
    public File getFile() {
        return this.file;
    }

    @Override
    public File getSource() {
        return this.source;
    }

    @Override
    public File getJavadoc() {
        return this.javadoc;
    }

    @Override
    public boolean isExported() {
        return this.exported;
    }

    public static DefaultOmniExternalDependency from(EclipseExternalDependency externalDependency) {
        return new DefaultOmniExternalDependency(
                externalDependency.getFile(),
                externalDependency.getSource(),
                externalDependency.getJavadoc(),
                getIsExported(externalDependency),
                getClasspathAttributes(externalDependency),
                getAccessRules(externalDependency));
    }

    /**
     * ExternalDependency#isExported is only available in Gradle versions >= 2.5.
     *
     * @param externalDependency the external dependency model
     */
    private static boolean getIsExported(ExternalDependency externalDependency) {
        try {
            return externalDependency.isExported();
        } catch (Exception ignore) {
            return true;
        }
    }
}
