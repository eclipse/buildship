/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.gradle.tooling.BuildAction;

final class CacheKey {

    private final boolean invalid;
    private final BuildAction<?> buildAction;
    private final Class<?> modelType;
    private final List<String> tasks;
    private final Map<String, String> envVariables;
    private final File javaHome;
    private final List<String> arguments;
    private final List<String> jvmArguments;
    private final Map<String, String> systemProperties;

    private CacheKey(Builder cacheKeyBuilder) {
        this.invalid = cacheKeyBuilder.invalid;
        this.buildAction = cacheKeyBuilder.buildAction;
        this.modelType = cacheKeyBuilder.modelType;
        this.tasks = cacheKeyBuilder.tasks;
        this.envVariables = cacheKeyBuilder.envVariables;
        this.javaHome = cacheKeyBuilder.javaHome;
        this.arguments = cacheKeyBuilder.arguments;
        this.jvmArguments = cacheKeyBuilder.jvmArguments;
        this.systemProperties = cacheKeyBuilder.systemProperties;
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                this.buildAction,
                this.modelType,
                this.invalid,
                this.tasks,
                this.envVariables,
                this.javaHome,
                this.arguments,
                this.jvmArguments,
                this.systemProperties
            );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CacheKey other = (CacheKey) obj;
        return Objects.equals(this.buildAction, other.buildAction)
                && Objects.equals(this.modelType, other.modelType)
                && Objects.equals(this.invalid, other.invalid)
                && Objects.equals(this.tasks, other.tasks)
                && Objects.equals(this.envVariables, other.envVariables)
                && Objects.equals(this.javaHome, other.javaHome)
                && Objects.equals(this.arguments, other.arguments)
                && Objects.equals(this.jvmArguments, other.jvmArguments)
                && Objects.equals(this.systemProperties, other.systemProperties);
    }

    static final class Builder {

        private boolean invalid = false;
        private BuildAction<?> buildAction;
        private Class<?> modelType;
        private List<String> tasks;
        private Map<String, String> envVariables;
        private File javaHome;
        private List<String> arguments;
        private List<String> jvmArguments;
        private Map<String, String> systemProperties;

        private Builder() {
        }

        public void markInvalid() {
            this.invalid = true;
        }

        public Builder setBuildAction(BuildAction<?> buildAction) {
            this.buildAction = buildAction;
            return this;
        }

        public Builder setModelType(Class<?> modelType) {
            this.modelType = modelType;
            return this;
        }

        public Builder setTasks(List<String> tasks) {
            this.tasks = tasks;
            return this;
        }

        public Builder setEnvironmentVariables(Map<String, String> envVariables) {
            this.envVariables = envVariables;
            return this;
        }

        public Builder setJavaHome(File javaHome) {
            this.javaHome = javaHome.getAbsoluteFile();
            return this;
        }

        public Builder setArguments(List<String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public Builder addArguments(List<String> arguments) {
            List<String> newArguments = this.arguments == null ? new ArrayList<>() : new ArrayList<>(this.arguments);
            newArguments.addAll(arguments);
            this.arguments = newArguments;
            return this;
        }

        public Builder setJvmArguments(List<String> jvmArguments) {
            this.jvmArguments = jvmArguments;
            return this;
        }

        public Builder addJvmArguments(List<String> jvmArguments) {
            List<String> newJvmArguments = this.jvmArguments == null ? new ArrayList<>() : new ArrayList<>(this.jvmArguments);
            newJvmArguments.addAll(jvmArguments);
            this.jvmArguments = newJvmArguments;
            return this;
        }

        public Builder withSystemPrperties(Map<String, String> systemProperties) {
            this.systemProperties = systemProperties;
            return this;
        }

        public CacheKey build() {
            return new CacheKey(this);
        }
    }

}
