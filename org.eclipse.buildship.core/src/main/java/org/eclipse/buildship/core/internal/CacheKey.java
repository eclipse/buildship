/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.gradle.tooling.BuildAction;

class CacheKey {
    private boolean invalid = false;
    private BuildAction<?> buildAction;
    private Class<?> modelType;
    private List<String> tasks;
    private Map<String, String> envVariables;
    private File javaHome;
    private List<String> arguments;
    private List<String> jvmArguments;

    public boolean isInvalid() {
        return this.invalid;
    }

    public void markInvalid() {
        this.invalid = true;
    }

    public void setBuildAction(BuildAction<?> buildAction) {
        this.buildAction = buildAction;
    }

    public void setModelType(Class<?> modelType) {
        this.modelType = modelType;
    }

    public void setTasks(List<String> tasks) {
        this.tasks = tasks;
    }

    public void setEnvironmentVariables(Map<String, String> envVariables) {
        this.envVariables = envVariables;
    }

    public void setJavaHome(File javaHome) {
        this.javaHome = javaHome.getAbsoluteFile();
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public void addArguments(List<String> arguments) {
        List<String> newArguments = new ArrayList<>(this.arguments);
        newArguments.addAll(arguments);
        this.arguments = newArguments;
    }

    public void setJvmArguments(List<String> jvmArguments) {
        this.jvmArguments = jvmArguments;
    }

    public void addJvmArguments(List<String> jvmArguments) {
        List<String> newJvmArguments = new ArrayList<>(this.jvmArguments);
        newJvmArguments.addAll(jvmArguments);
        this.jvmArguments = newJvmArguments;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.buildAction, this.modelType, this.invalid, this.tasks, this.envVariables, this.javaHome, this.arguments, this.jvmArguments);
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
                && Objects.equals(this.jvmArguments, other.jvmArguments);
    }
}
