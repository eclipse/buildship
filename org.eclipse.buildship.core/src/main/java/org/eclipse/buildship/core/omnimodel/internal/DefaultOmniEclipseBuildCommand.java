/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import java.util.Map;

import org.gradle.tooling.model.eclipse.EclipseBuildCommand;

import com.google.common.collect.ImmutableMap;

import org.eclipse.buildship.core.omnimodel.OmniEclipseBuildCommand;

/**
 * Default implementation of the {@link OmniEclipseBuildCommand} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniEclipseBuildCommand implements OmniEclipseBuildCommand {

    private final String name;
    private final Map<String, String> arguments;

    private DefaultOmniEclipseBuildCommand(String name, Map<String, String> arguments) {
        this.name = name;
        this.arguments = ImmutableMap.copyOf(arguments);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Map<String, String> getArguments() {
        return this.arguments;
    }

    public static DefaultOmniEclipseBuildCommand from(EclipseBuildCommand buildCommand) {
        return new DefaultOmniEclipseBuildCommand(buildCommand.getName(), buildCommand.getArguments());
    }

}
