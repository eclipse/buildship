/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.google.common.base.Preconditions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.buildship.core.internal.configuration.CompositeConfiguration;

public class CompositeProperties {

    private final File compositePreferencesDir;

    private final IAdaptable[] projectList;
    private final Boolean overwriteWorkspaceSettings;
    private final GradleDistribution distribution;
    private final File gradleUserHome;
    private final File javaHome;
    private final Boolean buildScansEnabled;
    private final Boolean offlineMode;
    private final Boolean autoSync;
    private final List<String> arguments;
    private final List<String> jvmArguments;
    private final Boolean showConsoleView;
    private final Boolean showExecutionsView;
    private final Boolean useProjectAsRoot;
    private final File rootProject;

    private static final String KEY_COMPOSITE_PROJECTS = "composite.projects";
    private static final String KEY_OVERWRITE_WORKSPACE_SETTINGS = "override.workspace.settings";
    private static final String KEY_DISTRIBUTION = "connection.gradle.distribution";
    private static final String KEY_GRADLE_USER_HOME = "gradle.user.home";
    private static final String KEY_JAVA_HOME = "java.home";
    private static final String KEY_BUILD_SCANS_ENABLED = "build.scans.enabled";
    private static final String KEY_OFFLINE_MODE = "offline.mode";
    private static final String KEY_AUTO_SYNC = "auto.sync";
    private static final String KEY_ARGUMENTS = "arguments";
    private static final String KEY_JVM_ARGUMENTS = "jvm.arguments";
    private static final String KEY_SHOW_CONSOLE_VIEW = "show.console.view";
    private static final String KEY_SHOW_EXECUTION_VIEW = "show.executions.view";
    private static final String KEY_USE_PROJECT_AS_ROOT = "project.as.root";
    private static final String KEY_ROOT_PROJECT = "root.project";

    private CompositeProperties(CompositePropertiesBuilder builder) {
        this.compositePreferencesDir = Preconditions.checkNotNull(builder.compositeDirectory);
        this.projectList = builder.projectList;
        this.overwriteWorkspaceSettings = builder.overrideWorkspaceConfiguration;
        this.distribution = builder.gradleDistribution == null ? GradleDistribution.fromBuild() : builder.gradleDistribution;
        this.gradleUserHome = builder.gradleUserHome == null ? null: builder.gradleUserHome;
        this.javaHome = builder.javaHome == null ? null : builder.javaHome;
        this.buildScansEnabled = builder.buildScansEnabled;
        this.offlineMode = builder.offlineMode;
        this.autoSync = builder.autoSync;
        this.arguments = builder.arguments == null ? Collections.emptyList() : builder.arguments;
        this.jvmArguments = builder.jvmArguments == null ? Collections.emptyList() : builder.jvmArguments;
        this.showConsoleView = builder.showConsoleView;
        this.showExecutionsView = builder.showExecutionsView;
        this.useProjectAsRoot = builder.projectAsCompositeRoot;
        this.rootProject = builder.rootProject == null ? null: builder.rootProject;


    }

    public static CompositePropertiesReader getCompositeReaderForFile(File compositeProperties) {
        return new CompositePropertiesReader(compositeProperties);
    }

    public static CompositePropertiesBuilder forRootProjectDirectory(File rootProjectDirectory) {
        return new CompositePropertiesBuilder(rootProjectDirectory);
    }

    public static CompositePropertiesBuilder forCompositeConfiguration(CompositeConfiguration compositeConf) {
        return new CompositePropertiesBuilder(compositeConf);
    }

    public Properties toProperties() {
        Properties prop = new Properties();

        prop.put(KEY_COMPOSITE_PROJECTS, getProjectString(this.projectList));
        prop.put(KEY_OVERWRITE_WORKSPACE_SETTINGS, this.overwriteWorkspaceSettings.toString());
        prop.put(KEY_DISTRIBUTION, this.distribution == null ? GradleDistribution.fromBuild() : this.distribution.toString());
        prop.put(KEY_GRADLE_USER_HOME, this.gradleUserHome == null ? "" : this.gradleUserHome.getAbsolutePath());
        prop.put(KEY_JAVA_HOME, this.javaHome == null ? "" : this.javaHome.getAbsolutePath());
        prop.put(KEY_BUILD_SCANS_ENABLED, this.buildScansEnabled.toString());
        prop.put(KEY_OFFLINE_MODE, this.offlineMode.toString());
        prop.put(KEY_AUTO_SYNC, this.autoSync.toString());
        prop.put(KEY_ARGUMENTS, removeBrackets(this.arguments.toString()));
        prop.put(KEY_JVM_ARGUMENTS, removeBrackets(this.jvmArguments.toString()));
        prop.put(KEY_SHOW_CONSOLE_VIEW, this.showConsoleView.toString());
        prop.put(KEY_SHOW_EXECUTION_VIEW, this.showExecutionsView.toString());
        prop.put(KEY_USE_PROJECT_AS_ROOT, this.useProjectAsRoot.toString());
        prop.put(KEY_ROOT_PROJECT, this.rootProject == null ? "" : this.rootProject.toString());
        return prop;
    }

    private String getProjectString(IAdaptable[] projects) {
        StringBuilder sb = new StringBuilder();
        for (Iterator<IAdaptable> it = Arrays.asList(projects).iterator(); it.hasNext();) {
            IAdaptable project = it.next();
            if (project instanceof IProject) {
                if (it.hasNext()) {
                    sb.append(((IProject) project).getName() + ", ");
                } else {
                    sb.append(((IProject) project).getName());
                }
            } else if (project instanceof IResource) {
                IResource externalProject = (IResource) project;
                System.out.println(externalProject.getName());
            }
        }
        return sb.toString();
    }

    private String removeBrackets(String arguments) {
        return arguments.replace("[", "").replace("]", "").replace(",", "");
    }

    public static final class CompositePropertiesBuilder {

        private final File compositeDirectory;
        public IAdaptable[] projectList;
        private boolean overrideWorkspaceConfiguration = false;
        private GradleDistribution gradleDistribution;
        private File gradleUserHome = null;
        private File javaHome = null;
        private boolean buildScansEnabled = false;
        private boolean offlineMode = false;
        private boolean autoSync = false;
        private List<String> arguments = new ArrayList<>();
        private List<String> jvmArguments = new ArrayList<>();
        private boolean showConsoleView = true;
        private boolean showExecutionsView = true;
        private boolean projectAsCompositeRoot = false;
        private File rootProject = null;

        private CompositePropertiesBuilder(File compositeDirectory) {
            this.compositeDirectory = compositeDirectory;
        }

        public CompositePropertiesBuilder(CompositeConfiguration compositeConf) {
            this.compositeDirectory = compositeConf.getCompositeDir();
            this.projectList = compositeConf.getProjectList();
            this.overrideWorkspaceConfiguration = compositeConf.getBuildConfiguration().isOverrideWorkspaceSettings();
            this.gradleDistribution = compositeConf.getBuildConfiguration().getGradleDistribution();
            this.gradleUserHome = compositeConf.getBuildConfiguration().getGradleUserHome();
            this.javaHome = compositeConf.getBuildConfiguration().getJavaHome();
            this.buildScansEnabled = compositeConf.getBuildConfiguration().isBuildScansEnabled();
            this.offlineMode = compositeConf.getBuildConfiguration().isOfflineMode();
            this.autoSync = compositeConf.getBuildConfiguration().isAutoSync();
            this.arguments = compositeConf.getBuildConfiguration().getArguments();
            this.jvmArguments = compositeConf.getBuildConfiguration().getJvmArguments();
            this.showConsoleView = compositeConf.getBuildConfiguration().isShowConsoleView();
            this.showExecutionsView = compositeConf.getBuildConfiguration().isShowExecutionsView();
            this.projectAsCompositeRoot = compositeConf.projectAsCompositeRoot();
            this.rootProject = compositeConf.getRootProject();

        }

        public CompositePropertiesBuilder projectList(IAdaptable[] projectList) {
            this.projectList = projectList;
            return this;
        }

        public CompositePropertiesBuilder overrideWorkspaceConfiguration(boolean overrideWorkspaceConfiguration) {
            this.overrideWorkspaceConfiguration = overrideWorkspaceConfiguration;
            return this;
        }

        public CompositePropertiesBuilder gradleDistribution(GradleDistribution gradleDistribution) {
            this.gradleDistribution = gradleDistribution;
            return this;
        }

        public CompositePropertiesBuilder gradleUserHome(File gradleUserHome) {
            this.gradleUserHome = gradleUserHome;
            return this;
        }

        public CompositePropertiesBuilder javaHome(File javaHome) {
            this.javaHome = javaHome;
            return this;
        }

        public CompositePropertiesBuilder buildScansEnabled(boolean buildScansEnabled) {
            this.buildScansEnabled = buildScansEnabled;
            return this;
        }

        public CompositePropertiesBuilder offlineMode(boolean offlineMode) {
            this.offlineMode = offlineMode;
            return this;
        }

        public CompositePropertiesBuilder autoSync(boolean autoSync) {
            this.autoSync = autoSync;
            return this;
        }

        public CompositePropertiesBuilder arguments(List<String> arguments) {
            this.arguments = arguments;
            return this;
        }

        public CompositePropertiesBuilder jvmArguments(List<String> jvmArguments) {
            this.jvmArguments = jvmArguments;
            return this;
        }

        public CompositePropertiesBuilder showConsoleView(boolean showConsoleView) {
            this.showConsoleView = showConsoleView;
            return this;
        }

        public CompositePropertiesBuilder showExecutionsView(boolean showExecutionsView) {
            this.showExecutionsView = showExecutionsView;
            return this;
        }

        public CompositePropertiesBuilder projectAsCompositeRoot(boolean projectAsCompositeRoot) {
            this.projectAsCompositeRoot = projectAsCompositeRoot;
            return this;
        }

        public CompositePropertiesBuilder rootProject(File rootProject) {
            this.rootProject = rootProject;
            return this;
        }

        public CompositeProperties build() {
            return new CompositeProperties(this);
        }
    }

    public static final class CompositePropertiesReader {
        private final File compositeDirectory;
        private Properties compositeProperties = new Properties();

        private CompositePropertiesReader(File compositeDirectory) {
            this.compositeDirectory = compositeDirectory;
              try {
                FileInputStream input= new FileInputStream(this.compositeDirectory);
                this.compositeProperties = new Properties();
                this.compositeProperties.load(input);
            } catch (IOException e) {
               e.printStackTrace();
            }
        }

        public boolean getProjectAsCompositeRoot() {
            return Boolean.valueOf(this.compositeProperties.get(KEY_USE_PROJECT_AS_ROOT).toString());
        }

        public File getRootProject() {
            return new File(this.compositeProperties.get(KEY_ROOT_PROJECT).toString());
        }

        //If needed implement reader for other properties

    }
}
