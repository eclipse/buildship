/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
