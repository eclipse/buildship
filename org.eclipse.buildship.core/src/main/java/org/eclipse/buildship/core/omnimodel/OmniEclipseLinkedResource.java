/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * Describes a linked resource which is a file or folder that is stored in a location in the file system outside of the project's location.
 *
 * @author Etienne Studer
 * @see org.gradle.tooling.model.eclipse.EclipseLinkedResource
 */
public interface OmniEclipseLinkedResource {

    /**
     * The project-relative path of the linked resource as it appears in the workspace. <p> See the official Eclipse documentation for most up-to-date information on properties of
     * a linked resource <p> For example, a linked resource to a file system folder /some/path/to/someFolder can have a name 'someFolder'
     *
     * @return the name
     */
    String getName();

    /**
     * The resource type. <p> If 'location' property is used the values are: "1" for a file, or "2" for a folder. <p> If 'locationUri' property is used then the values are: "1" for
     * file or folder when 'locationUri' first segment is a workspace path variable (or path variable navigation element), "2" for an eclipse virtual folder. <p> See the official
     * Eclipse documentation for most up-to-date information on properties of a linked resource.
     *
     * @return the Eclipse link type
     */
    String getType();

    /**
     * The local file system absolute path of the target of the linked resource. For example: '/path/to/somewhere'. Mutually exclusive with 'locationUri'. <p> See the official
     * Eclipse documentation for most up-to-date information on properties of a linked resource.
     *
     * @return the location
     */
    String getLocation();

    /**
     * If the file is not in the local file system, this attribute contains the absolute URI of the resource in some backing file system. Mutually exclusive with 'location'. <p>
     * When a workspace path variable is used as part of the path then this property must be used instead of 'location'. <p> Used for virtual folders. In that case the value is:
     * 'virtual:/virtual'. <p> See the official Eclipse documentation for most up-to-date information on properties of a linked resource.
     *
     * @return the location uri
     */
    String getLocationUri();

}
