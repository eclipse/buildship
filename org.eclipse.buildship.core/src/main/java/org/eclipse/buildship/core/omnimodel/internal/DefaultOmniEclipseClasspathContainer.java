/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.util.List;

import org.gradle.tooling.model.eclipse.EclipseClasspathContainer;

import com.google.common.base.Optional;

import org.eclipse.buildship.core.omnimodel.OmniAccessRule;
import org.eclipse.buildship.core.omnimodel.OmniClasspathAttribute;
import org.eclipse.buildship.core.omnimodel.OmniEclipseClasspathContainer;

/**
 * Default implementation of the {@link OmniEclipseClasspathContainer} interface.
 *
 * @author Donat Csikos
 */
public class DefaultOmniEclipseClasspathContainer extends AbstractOmniClasspathEntry implements OmniEclipseClasspathContainer {

    private final String path;
    private final boolean isExported;

    private DefaultOmniEclipseClasspathContainer(String path, boolean isExported, Optional<List<OmniClasspathAttribute>> attributes, Optional<List<OmniAccessRule>> accessRules) {
        super(attributes, accessRules);
        this.path = path;
        this.isExported = isExported;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public boolean isExported() {
        return this.isExported;
    }

    public static DefaultOmniEclipseClasspathContainer from(EclipseClasspathContainer container) {
        return new DefaultOmniEclipseClasspathContainer(
                container.getPath(),
                container.isExported(),
                getClasspathAttributes(container),
                getAccessRules(container));
    }

}
