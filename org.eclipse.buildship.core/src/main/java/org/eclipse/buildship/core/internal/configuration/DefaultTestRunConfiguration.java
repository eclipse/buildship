/*
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.configuration;

import java.util.List;

/**
 * Default implementation for {@link TestRunConfiguration}.
 */
class DefaultTestRunConfiguration extends AbstractRunConfiguration<TestRunConfigurationProperties> implements TestRunConfiguration {

    public DefaultTestRunConfiguration(ProjectConfiguration projectConfiguration, TestRunConfigurationProperties properties) {
        super(projectConfiguration, properties);
    }

    @Override
    public List<String> getTests() {
        return this.properties.getTests();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultTestRunConfiguration) {
            super.equals(obj);
        }
        return false;
    }
}
