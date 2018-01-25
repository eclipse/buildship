/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import org.gradle.tooling.model.eclipse.EclipseLinkedResource;

import org.eclipse.buildship.core.omnimodel.OmniEclipseLinkedResource;

/**
 * Default implementation of the {@link OmniEclipseLinkedResource} interface.
 *
 * @author Etienne Studer
 */
public final class DefaultOmniEclipseLinkedResource implements OmniEclipseLinkedResource {

    private final String name;
    private final String type;
    private final String location;
    private final String locationUri;

    private DefaultOmniEclipseLinkedResource(String name, String type, String location, String locationUri) {
        this.name = name;
        this.type = type;
        this.location = location;
        this.locationUri = locationUri;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getLocation() {
        return this.location;
    }

    @Override
    public String getLocationUri() {
        return this.locationUri;
    }

    public static DefaultOmniEclipseLinkedResource from(EclipseLinkedResource linkedResource) {
        return new DefaultOmniEclipseLinkedResource(
                linkedResource.getName(),
                linkedResource.getType(),
                linkedResource.getLocation(),
                linkedResource.getLocationUri());
    }

}
