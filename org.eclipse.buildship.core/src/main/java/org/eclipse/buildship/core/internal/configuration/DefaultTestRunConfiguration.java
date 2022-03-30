/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
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
 * Default implementation for {@link TestRunConfiguration}.
 */
class DefaultTestRunConfiguration extends AbstractRunConfiguration<TestRunConfigurationProperties> implements TestRunConfiguration {

    public DefaultTestRunConfiguration(ProjectConfiguration projectConfiguration, TestRunConfigurationProperties properties) {
        super(projectConfiguration, properties);
    }

    @Override
    public List<Test> getTests() {
        return this.properties.getTests();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DefaultTestRunConfiguration) {
            super.equals(obj);
        }
        return false;
    }

    @Override
    public String getTestTask() {
        return this.properties.getTestTask();
    }

    @Override
    public List<String> getTestClasses() {
        return this.properties.getTestClasses();
    }

    @Override
    public List<String> getTestMethods() {
        return this.properties.getTestMethods();
    }

    @Override
    public List<String> getTestPackages() {
        return this.properties.getTestPackages();
    }

    @Override
    public List<String> getTestPatterns() {
        return this.properties.getTestPatterns();
    }
}
