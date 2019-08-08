/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import java.util.List;

/**
 * Default implementation for {@link RunConfiguration}.
 */
class DefaultRunConfiguration extends AbstractRunConfiguration<RunConfigurationProperties> implements RunConfiguration {

    public DefaultRunConfiguration(ProjectConfiguration projectConfiguration, RunConfigurationProperties properties) {
        super(projectConfiguration, properties);
    }

    RunConfigurationProperties getProperties() {
        return this.properties;
    }

    @Override
    public List<String> getTasks() {
        return this.properties.getTasks();
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultRunConfiguration) {
            return super.equals(obj);
        }
        return false;
    }
}
