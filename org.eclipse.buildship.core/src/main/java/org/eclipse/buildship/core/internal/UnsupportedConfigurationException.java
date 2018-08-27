/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.internal;

/**
 * Custom unchecked exception for indicating that the configuration provided by Gradle
 * can't be properly synchronized with Eclipse.
 */
public final class UnsupportedConfigurationException extends GradlePluginsRuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedConfigurationException(String message) {
        super(message);
    }

}
