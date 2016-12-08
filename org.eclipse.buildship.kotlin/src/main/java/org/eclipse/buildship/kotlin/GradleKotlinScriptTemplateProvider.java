/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.kotlin.core.model.ScriptTemplateProviderEx;
import org.jetbrains.kotlin.script.ScriptDependenciesResolver;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.launching.JavaRuntime;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.configuration.WorkspaceConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;

/**
 * Contributes the Gradle Kotlin Script template to the Kotlin Eclipse integration.
 *
 * @author Donat Csikos
 */
public class GradleKotlinScriptTemplateProvider implements ScriptTemplateProviderEx {

    @Override
    public Iterable<String> getTemplateClassClasspath() {
        return KotlinPlugin.templateClasspath();
    }

    @Override
    public Map<String, Object> getEnvironment(IFile file) {
        HashMap<String, Object> environment = new HashMap<String, Object>();

        WorkspaceConfiguration workspaceConfig = CorePlugin.workspaceConfigurationManager().loadWorkspaceConfiguration();
        ProjectConfiguration projectConfig = CorePlugin.projectConfigurationManager().readProjectConfiguration(file.getProject());
        GradleDistributionWrapper distribution = GradleDistributionWrapper.from(projectConfig.getGradleDistribution());

        environment.put("rtPath", rtPath());
        environment.put("rootProject", projectConfig.getRootProjectDirectory());
        environment.put("distributionType", distribution.getType().name());
        environment.put("distributionConfig", distribution.getConfiguration());
        environment.put("gradleUserHome", workspaceConfig.getGradleUserHome());
        environment.put("isOffline", workspaceConfig.isOffline());

        return environment;
    }

    @Override
    public String getTemplateClassName() {
        return "org.eclipse.buildship.kotlin.KotlinBuildScript";
    }

    @Override
    public ScriptDependenciesResolver getResolver() {
        // NOTE: This resolver will be executed in Eclipse, but it doesn't work now.
        return null;
    }

    @Override
    public boolean isApplicable(IFile file) {
        return CorePlugin.projectConfigurationManager().tryReadProjectConfiguration(file.getProject()).isPresent();
    }

    private static List<File> rtPath() {
        File rtJar = new File(JavaRuntime.getDefaultVMInstall().getInstallLocation(), "jre/lib/rt.jar");
        return rtJar.exists() ? Arrays.asList(rtJar) : Collections.<File>emptyList();
    }

}