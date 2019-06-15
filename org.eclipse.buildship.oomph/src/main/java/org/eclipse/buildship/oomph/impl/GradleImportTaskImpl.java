/**
 * Copyright (c) 2019 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.oomph.impl;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;
import org.eclipse.oomph.resources.SourceLocator;
import org.eclipse.oomph.setup.SetupTaskContext;
import org.eclipse.oomph.setup.Trigger;
import org.eclipse.oomph.setup.impl.SetupTaskImpl;
import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.BuildConfiguration;
import org.eclipse.buildship.core.BuildConfiguration.BuildConfigurationBuilder;
import org.eclipse.buildship.core.GradleBuild;
import org.eclipse.buildship.core.GradleCore;
import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.GradleWorkspace;
import org.eclipse.buildship.oomph.DistributionType;
import org.eclipse.buildship.oomph.GradleImportPackage;
import org.eclipse.buildship.oomph.GradleImportTask;
import org.eclipse.buildship.oomph.ImportTaskMessages;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Task</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getSourceLocators <em>Source
 * Locators</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#isOverrideWorkspaceSettings
 * <em>Override Workspace Settings</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getDistributionType
 * <em>Distribution Type</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getLocalInstallationDirectory
 * <em>Local Installation Directory</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getRemoteDistributionLocation
 * <em>Remote Distribution Location</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getSpecificGradleVersion
 * <em>Specific Gradle Version</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getProgramArguments <em>Program
 * Arguments</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getJvmArguments <em>Jvm
 * Arguments</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getGradleUserHome <em>Gradle
 * User Home</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#getJavaHome <em>Java
 * Home</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#isOfflineMode <em>Offline
 * Mode</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#isBuildScans <em>Build
 * Scans</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#isAutomaticProjectSynchronization
 * <em>Automatic Project Synchronization</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#isShowConsoleView <em>Show
 * Console View</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl#isShowExecutionsView <em>Show
 * Executions View</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GradleImportTaskImpl extends SetupTaskImpl implements GradleImportTask {

    private static final IWorkspaceRoot ROOT = ResourcesPlugin.getWorkspace().getRoot();

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final String copyright = "Copyright (c) 2019 the original author or authors.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n";

    /**
     * The cached value of the '{@link #getSourceLocators() <em>Source Locators</em>}' containment
     * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getSourceLocators()
     * @generated
     * @ordered
     */
    protected EList<SourceLocator> sourceLocators;

    /**
     * The default value of the '{@link #isOverrideWorkspaceSettings() <em>Override Workspace
     * Settings</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isOverrideWorkspaceSettings()
     * @generated
     * @ordered
     */
    protected static final boolean OVERRIDE_WORKSPACE_SETTINGS_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isOverrideWorkspaceSettings() <em>Override Workspace
     * Settings</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isOverrideWorkspaceSettings()
     * @generated
     * @ordered
     */
    protected boolean overrideWorkspaceSettings = OVERRIDE_WORKSPACE_SETTINGS_EDEFAULT;

    /**
     * The default value of the '{@link #getDistributionType() <em>Distribution Type</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDistributionType()
     * @generated
     * @ordered
     */
    protected static final DistributionType DISTRIBUTION_TYPE_EDEFAULT = DistributionType.GRADLE_WRAPPER;

    /**
     * The cached value of the '{@link #getDistributionType() <em>Distribution Type</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDistributionType()
     * @generated
     * @ordered
     */
    protected DistributionType distributionType = DISTRIBUTION_TYPE_EDEFAULT;

    /**
     * The default value of the '{@link #getLocalInstallationDirectory() <em>Local Installation
     * Directory</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getLocalInstallationDirectory()
     * @generated
     * @ordered
     */
    protected static final String LOCAL_INSTALLATION_DIRECTORY_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getLocalInstallationDirectory() <em>Local Installation
     * Directory</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getLocalInstallationDirectory()
     * @generated
     * @ordered
     */
    protected String localInstallationDirectory = LOCAL_INSTALLATION_DIRECTORY_EDEFAULT;

    /**
     * The default value of the '{@link #getRemoteDistributionLocation() <em>Remote Distribution
     * Location</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getRemoteDistributionLocation()
     * @generated
     * @ordered
     */
    protected static final String REMOTE_DISTRIBUTION_LOCATION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getRemoteDistributionLocation() <em>Remote Distribution
     * Location</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getRemoteDistributionLocation()
     * @generated
     * @ordered
     */
    protected String remoteDistributionLocation = REMOTE_DISTRIBUTION_LOCATION_EDEFAULT;

    /**
     * The default value of the '{@link #getSpecificGradleVersion() <em>Specific Gradle
     * Version</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getSpecificGradleVersion()
     * @generated
     * @ordered
     */
    protected static final String SPECIFIC_GRADLE_VERSION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getSpecificGradleVersion() <em>Specific Gradle
     * Version</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getSpecificGradleVersion()
     * @generated
     * @ordered
     */
    protected String specificGradleVersion = SPECIFIC_GRADLE_VERSION_EDEFAULT;

    /**
     * The cached value of the '{@link #getProgramArguments() <em>Program Arguments</em>}' attribute
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getProgramArguments()
     * @generated
     * @ordered
     */
    protected EList<String> programArguments;

    /**
     * The cached value of the '{@link #getJvmArguments() <em>Jvm Arguments</em>}' attribute list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getJvmArguments()
     * @generated
     * @ordered
     */
    protected EList<String> jvmArguments;

    /**
     * The default value of the '{@link #getGradleUserHome() <em>Gradle User Home</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getGradleUserHome()
     * @generated
     * @ordered
     */
    protected static final String GRADLE_USER_HOME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getGradleUserHome() <em>Gradle User Home</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getGradleUserHome()
     * @generated
     * @ordered
     */
    protected String gradleUserHome = GRADLE_USER_HOME_EDEFAULT;

    /**
     * The default value of the '{@link #getJavaHome() <em>Java Home</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #getJavaHome()
     * @generated
     * @ordered
     */
    protected static final String JAVA_HOME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getJavaHome() <em>Java Home</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #getJavaHome()
     * @generated
     * @ordered
     */
    protected String javaHome = JAVA_HOME_EDEFAULT;

    /**
     * The default value of the '{@link #isOfflineMode() <em>Offline Mode</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #isOfflineMode()
     * @generated
     * @ordered
     */
    protected static final boolean OFFLINE_MODE_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isOfflineMode() <em>Offline Mode</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #isOfflineMode()
     * @generated
     * @ordered
     */
    protected boolean offlineMode = OFFLINE_MODE_EDEFAULT;

    /**
     * The default value of the '{@link #isBuildScans() <em>Build Scans</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #isBuildScans()
     * @generated
     * @ordered
     */
    protected static final boolean BUILD_SCANS_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isBuildScans() <em>Build Scans</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @see #isBuildScans()
     * @generated
     * @ordered
     */
    protected boolean buildScans = BUILD_SCANS_EDEFAULT;

    /**
     * The default value of the '{@link #isAutomaticProjectSynchronization() <em>Automatic Project
     * Synchronization</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isAutomaticProjectSynchronization()
     * @generated
     * @ordered
     */
    protected static final boolean AUTOMATIC_PROJECT_SYNCHRONIZATION_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isAutomaticProjectSynchronization() <em>Automatic Project
     * Synchronization</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isAutomaticProjectSynchronization()
     * @generated
     * @ordered
     */
    protected boolean automaticProjectSynchronization = AUTOMATIC_PROJECT_SYNCHRONIZATION_EDEFAULT;

    /**
     * The default value of the '{@link #isShowConsoleView() <em>Show Console View</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isShowConsoleView()
     * @generated
     * @ordered
     */
    protected static final boolean SHOW_CONSOLE_VIEW_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isShowConsoleView() <em>Show Console View</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isShowConsoleView()
     * @generated
     * @ordered
     */
    protected boolean showConsoleView = SHOW_CONSOLE_VIEW_EDEFAULT;

    /**
     * The default value of the '{@link #isShowExecutionsView() <em>Show Executions View</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isShowExecutionsView()
     * @generated
     * @ordered
     */
    protected static final boolean SHOW_EXECUTIONS_VIEW_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isShowExecutionsView() <em>Show Executions View</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isShowExecutionsView()
     * @generated
     * @ordered
     */
    protected boolean showExecutionsView = SHOW_EXECUTIONS_VIEW_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected GradleImportTaskImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return GradleImportPackage.Literals.GRADLE_IMPORT_TASK;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<SourceLocator> getSourceLocators() {
        if (sourceLocators == null) {
            sourceLocators = new EObjectContainmentEList<SourceLocator>(SourceLocator.class, this, GradleImportPackage.GRADLE_IMPORT_TASK__SOURCE_LOCATORS);
        }
        return sourceLocators;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isOverrideWorkspaceSettings() {
        return overrideWorkspaceSettings;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setOverrideWorkspaceSettings(boolean newOverrideWorkspaceSettings) {
        boolean oldOverrideWorkspaceSettings = overrideWorkspaceSettings;
        overrideWorkspaceSettings = newOverrideWorkspaceSettings;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS, oldOverrideWorkspaceSettings,
                    overrideWorkspaceSettings));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public DistributionType getDistributionType() {
        return distributionType;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setDistributionType(DistributionType newDistributionType) {
        DistributionType oldDistributionType = distributionType;
        distributionType = newDistributionType == null ? DISTRIBUTION_TYPE_EDEFAULT : newDistributionType;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE, oldDistributionType, distributionType));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getLocalInstallationDirectory() {
        return localInstallationDirectory;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setLocalInstallationDirectory(String newLocalInstallationDirectory) {
        String oldLocalInstallationDirectory = localInstallationDirectory;
        localInstallationDirectory = newLocalInstallationDirectory;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY, oldLocalInstallationDirectory,
                    localInstallationDirectory));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getRemoteDistributionLocation() {
        return remoteDistributionLocation;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setRemoteDistributionLocation(String newRemoteDistributionLocation) {
        String oldRemoteDistributionLocation = remoteDistributionLocation;
        remoteDistributionLocation = newRemoteDistributionLocation;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION, oldRemoteDistributionLocation,
                    remoteDistributionLocation));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getSpecificGradleVersion() {
        return specificGradleVersion;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setSpecificGradleVersion(String newSpecificGradleVersion) {
        String oldSpecificGradleVersion = specificGradleVersion;
        specificGradleVersion = newSpecificGradleVersion;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION, oldSpecificGradleVersion,
                    specificGradleVersion));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<String> getProgramArguments() {
        if (programArguments == null) {
            programArguments = new EDataTypeEList<String>(String.class, this, GradleImportPackage.GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS);
        }
        return programArguments;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<String> getJvmArguments() {
        if (jvmArguments == null) {
            jvmArguments = new EDataTypeEList<String>(String.class, this, GradleImportPackage.GRADLE_IMPORT_TASK__JVM_ARGUMENTS);
        }
        return jvmArguments;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getGradleUserHome() {
        return gradleUserHome;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setGradleUserHome(String newGradleUserHome) {
        String oldGradleUserHome = gradleUserHome;
        gradleUserHome = newGradleUserHome;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__GRADLE_USER_HOME, oldGradleUserHome, gradleUserHome));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getJavaHome() {
        return javaHome;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setJavaHome(String newJavaHome) {
        String oldJavaHome = javaHome;
        javaHome = newJavaHome;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__JAVA_HOME, oldJavaHome, javaHome));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isOfflineMode() {
        return offlineMode;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setOfflineMode(boolean newOfflineMode) {
        boolean oldOfflineMode = offlineMode;
        offlineMode = newOfflineMode;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__OFFLINE_MODE, oldOfflineMode, offlineMode));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isBuildScans() {
        return buildScans;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setBuildScans(boolean newBuildScans) {
        boolean oldBuildScans = buildScans;
        buildScans = newBuildScans;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__BUILD_SCANS, oldBuildScans, buildScans));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isAutomaticProjectSynchronization() {
        return automaticProjectSynchronization;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setAutomaticProjectSynchronization(boolean newAutomaticProjectSynchronization) {
        boolean oldAutomaticProjectSynchronization = automaticProjectSynchronization;
        automaticProjectSynchronization = newAutomaticProjectSynchronization;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION, oldAutomaticProjectSynchronization,
                    automaticProjectSynchronization));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isShowConsoleView() {
        return showConsoleView;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setShowConsoleView(boolean newShowConsoleView) {
        boolean oldShowConsoleView = showConsoleView;
        showConsoleView = newShowConsoleView;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW, oldShowConsoleView, showConsoleView));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isShowExecutionsView() {
        return showExecutionsView;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setShowExecutionsView(boolean newShowExecutionsView) {
        boolean oldShowExecutionsView = showExecutionsView;
        showExecutionsView = newShowExecutionsView;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW, oldShowExecutionsView, showExecutionsView));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case GradleImportPackage.GRADLE_IMPORT_TASK__SOURCE_LOCATORS:
                return ((InternalEList<?>) getSourceLocators()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case GradleImportPackage.GRADLE_IMPORT_TASK__SOURCE_LOCATORS:
                return getSourceLocators();
            case GradleImportPackage.GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS:
                return isOverrideWorkspaceSettings();
            case GradleImportPackage.GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE:
                return getDistributionType();
            case GradleImportPackage.GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY:
                return getLocalInstallationDirectory();
            case GradleImportPackage.GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION:
                return getRemoteDistributionLocation();
            case GradleImportPackage.GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION:
                return getSpecificGradleVersion();
            case GradleImportPackage.GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS:
                return getProgramArguments();
            case GradleImportPackage.GRADLE_IMPORT_TASK__JVM_ARGUMENTS:
                return getJvmArguments();
            case GradleImportPackage.GRADLE_IMPORT_TASK__GRADLE_USER_HOME:
                return getGradleUserHome();
            case GradleImportPackage.GRADLE_IMPORT_TASK__JAVA_HOME:
                return getJavaHome();
            case GradleImportPackage.GRADLE_IMPORT_TASK__OFFLINE_MODE:
                return isOfflineMode();
            case GradleImportPackage.GRADLE_IMPORT_TASK__BUILD_SCANS:
                return isBuildScans();
            case GradleImportPackage.GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION:
                return isAutomaticProjectSynchronization();
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW:
                return isShowConsoleView();
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW:
                return isShowExecutionsView();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case GradleImportPackage.GRADLE_IMPORT_TASK__SOURCE_LOCATORS:
                getSourceLocators().clear();
                getSourceLocators().addAll((Collection<? extends SourceLocator>) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS:
                setOverrideWorkspaceSettings((Boolean) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE:
                setDistributionType((DistributionType) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY:
                setLocalInstallationDirectory((String) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION:
                setRemoteDistributionLocation((String) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION:
                setSpecificGradleVersion((String) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS:
                getProgramArguments().clear();
                getProgramArguments().addAll((Collection<? extends String>) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__JVM_ARGUMENTS:
                getJvmArguments().clear();
                getJvmArguments().addAll((Collection<? extends String>) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__GRADLE_USER_HOME:
                setGradleUserHome((String) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__JAVA_HOME:
                setJavaHome((String) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__OFFLINE_MODE:
                setOfflineMode((Boolean) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__BUILD_SCANS:
                setBuildScans((Boolean) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION:
                setAutomaticProjectSynchronization((Boolean) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW:
                setShowConsoleView((Boolean) newValue);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW:
                setShowExecutionsView((Boolean) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case GradleImportPackage.GRADLE_IMPORT_TASK__SOURCE_LOCATORS:
                getSourceLocators().clear();
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS:
                setOverrideWorkspaceSettings(OVERRIDE_WORKSPACE_SETTINGS_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE:
                setDistributionType(DISTRIBUTION_TYPE_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY:
                setLocalInstallationDirectory(LOCAL_INSTALLATION_DIRECTORY_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION:
                setRemoteDistributionLocation(REMOTE_DISTRIBUTION_LOCATION_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION:
                setSpecificGradleVersion(SPECIFIC_GRADLE_VERSION_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS:
                getProgramArguments().clear();
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__JVM_ARGUMENTS:
                getJvmArguments().clear();
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__GRADLE_USER_HOME:
                setGradleUserHome(GRADLE_USER_HOME_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__JAVA_HOME:
                setJavaHome(JAVA_HOME_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__OFFLINE_MODE:
                setOfflineMode(OFFLINE_MODE_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__BUILD_SCANS:
                setBuildScans(BUILD_SCANS_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION:
                setAutomaticProjectSynchronization(AUTOMATIC_PROJECT_SYNCHRONIZATION_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW:
                setShowConsoleView(SHOW_CONSOLE_VIEW_EDEFAULT);
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW:
                setShowExecutionsView(SHOW_EXECUTIONS_VIEW_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case GradleImportPackage.GRADLE_IMPORT_TASK__SOURCE_LOCATORS:
                return sourceLocators != null && !sourceLocators.isEmpty();
            case GradleImportPackage.GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS:
                return overrideWorkspaceSettings != OVERRIDE_WORKSPACE_SETTINGS_EDEFAULT;
            case GradleImportPackage.GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE:
                return distributionType != DISTRIBUTION_TYPE_EDEFAULT;
            case GradleImportPackage.GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY:
                return LOCAL_INSTALLATION_DIRECTORY_EDEFAULT == null ? localInstallationDirectory != null
                        : !LOCAL_INSTALLATION_DIRECTORY_EDEFAULT.equals(localInstallationDirectory);
            case GradleImportPackage.GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION:
                return REMOTE_DISTRIBUTION_LOCATION_EDEFAULT == null ? remoteDistributionLocation != null
                        : !REMOTE_DISTRIBUTION_LOCATION_EDEFAULT.equals(remoteDistributionLocation);
            case GradleImportPackage.GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION:
                return SPECIFIC_GRADLE_VERSION_EDEFAULT == null ? specificGradleVersion != null : !SPECIFIC_GRADLE_VERSION_EDEFAULT.equals(specificGradleVersion);
            case GradleImportPackage.GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS:
                return programArguments != null && !programArguments.isEmpty();
            case GradleImportPackage.GRADLE_IMPORT_TASK__JVM_ARGUMENTS:
                return jvmArguments != null && !jvmArguments.isEmpty();
            case GradleImportPackage.GRADLE_IMPORT_TASK__GRADLE_USER_HOME:
                return GRADLE_USER_HOME_EDEFAULT == null ? gradleUserHome != null : !GRADLE_USER_HOME_EDEFAULT.equals(gradleUserHome);
            case GradleImportPackage.GRADLE_IMPORT_TASK__JAVA_HOME:
                return JAVA_HOME_EDEFAULT == null ? javaHome != null : !JAVA_HOME_EDEFAULT.equals(javaHome);
            case GradleImportPackage.GRADLE_IMPORT_TASK__OFFLINE_MODE:
                return offlineMode != OFFLINE_MODE_EDEFAULT;
            case GradleImportPackage.GRADLE_IMPORT_TASK__BUILD_SCANS:
                return buildScans != BUILD_SCANS_EDEFAULT;
            case GradleImportPackage.GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION:
                return automaticProjectSynchronization != AUTOMATIC_PROJECT_SYNCHRONIZATION_EDEFAULT;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW:
                return showConsoleView != SHOW_CONSOLE_VIEW_EDEFAULT;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW:
                return showExecutionsView != SHOW_EXECUTIONS_VIEW_EDEFAULT;
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy())
            return super.toString();

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (overrideWorkspaceSettings: ");
        result.append(overrideWorkspaceSettings);
        result.append(", distributionType: ");
        result.append(distributionType);
        result.append(", localInstallationDirectory: ");
        result.append(localInstallationDirectory);
        result.append(", remoteDistributionLocation: ");
        result.append(remoteDistributionLocation);
        result.append(", specificGradleVersion: ");
        result.append(specificGradleVersion);
        result.append(", programArguments: ");
        result.append(programArguments);
        result.append(", jvmArguments: ");
        result.append(jvmArguments);
        result.append(", gradleUserHome: ");
        result.append(gradleUserHome);
        result.append(", javaHome: ");
        result.append(javaHome);
        result.append(", offlineMode: ");
        result.append(offlineMode);
        result.append(", buildScans: ");
        result.append(buildScans);
        result.append(", automaticProjectSynchronization: ");
        result.append(automaticProjectSynchronization);
        result.append(", showConsoleView: ");
        result.append(showConsoleView);
        result.append(", showExecutionsView: ");
        result.append(showExecutionsView);
        result.append(')');
        return result.toString();
    }

    @Override
    public boolean isNeeded(SetupTaskContext context) throws Exception {
        EList<SourceLocator> sourceLocators = getSourceLocators();
        if (sourceLocators.isEmpty()) {
            return false;
        }
        if (context.getTrigger() != Trigger.MANUAL) {
            return !calculateProjectsToImport().isEmpty();
        }
        return true;
    }

    @Override
    public void perform(SetupTaskContext context) throws Exception {
        List<SourceLocator> sourceLocators = getSourceLocators();
        if (context.getTrigger() != Trigger.MANUAL) {
            // Only import new projects on startup triggers
            sourceLocators = calculateProjectsToImport();
        }
        SubMonitor monitor = SubMonitor.convert(context.getProgressMonitor(true));
        monitor.beginTask("", sourceLocators.size()); //$NON-NLS-1$
        try {
            GradleWorkspace workspace = GradleCore.getWorkspace();
            for (SourceLocator sourceLocator : sourceLocators) {
                context.log(NLS.bind(ImportTaskMessages.GradleImportTaskImpl_importing, sourceLocator.getRootFolder()));
                SubMonitor progress = monitor.newChild(1);
                Optional<GradleBuild> gradleBuildHolder = Optional.empty();

                for (IProject project : ROOT.getProjects()) {
                    if (!gradleBuildHolder.isPresent() && project.getLocation().equals(new Path(sourceLocator.getRootFolder()))) {
                        gradleBuildHolder = workspace.getBuild(project);
                        context.log(NLS.bind(ImportTaskMessages.GradleImportTaskImpl_found_existing, project.getName()));
                    }
                }
                GradleBuild gradleBuild = gradleBuildHolder.orElseGet(new GradleBuildSupplier(context, new File(sourceLocator.getRootFolder())));
                progress.setWorkRemaining(1);

                gradleBuild.synchronize(progress.newChild(1));
            }
        } finally {
            monitor.done();
        }
    }

    private List<SourceLocator> calculateProjectsToImport() {
        List<SourceLocator> projectsToImport = new ArrayList<>();
        for (SourceLocator sourceLocator : this.sourceLocators) {
            Path rootFolder = new Path(sourceLocator.getRootFolder());
            boolean projectPresentInWorkspace = false;
            for (IProject project : ROOT.getProjects()) {
                IPath projectFolder = project.getLocation();
                try {
                    if (Files.isSameFile(java.nio.file.Paths.get(projectFolder.toOSString()), java.nio.file.Paths.get(rootFolder.toOSString()))) {
                        projectPresentInWorkspace = true;
                        break;
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to compare project paths for equality", e);
                }
            }
            if (!projectPresentInWorkspace) {
                projectsToImport.add(sourceLocator);
            }
        }
        return projectsToImport;
    }

    private class GradleBuildSupplier implements Supplier<GradleBuild> {

        private final SetupTaskContext context;
        private final File rootFolder;

        public GradleBuildSupplier(SetupTaskContext context, File rootFolder) {
            this.context = context;
            this.rootFolder = rootFolder;
        }

        @Override
        public GradleBuild get() {
            GradleWorkspace workspace = GradleCore.getWorkspace();
            BuildConfigurationBuilder configBuilder = BuildConfiguration.forRootProjectDirectory(this.rootFolder);
            if (isOverrideWorkspaceSettings()) {
                configBuilder.overrideWorkspaceConfiguration(true);
                switch (getDistributionType()) {
                    case GRADLE_WRAPPER:
                        configBuilder.gradleDistribution(GradleDistribution.fromBuild());
                        break;
                    case LOCAL_INSTALLATION:
                        configBuilder.gradleDistribution(GradleDistribution.forLocalInstallation(new File(getLocalInstallationDirectory())));
                        break;
                    case REMOTE_DISTRIBUTION:
                        try {
                            configBuilder.gradleDistribution(GradleDistribution.forRemoteDistribution(new URI(getRemoteDistributionLocation())));
                        } catch (URISyntaxException e) {
                            throw new IllegalArgumentException("Invalid Gradle distribution uri " + getRemoteDistributionLocation(), e);
                        }
                        break;
                    case SPECIFIC_GRADLE_VERSION:
                        configBuilder.gradleDistribution(GradleDistribution.forVersion(getSpecificGradleVersion()));
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported distributionType " + getDistributionType());

                }
                if (getGradleUserHome() != null) {
                    configBuilder.gradleUserHome(new File(getGradleUserHome()));
                }
                if (getJavaHome() != null) {
                    configBuilder.javaHome(new File(getJavaHome()));
                }
                configBuilder.arguments(GradleImportTaskImpl.this.programArguments).jvmArguments(getJvmArguments()).offlineMode(isOfflineMode()).buildScansEnabled(isBuildScans())
                        .autoSync(isAutomaticProjectSynchronization()).showConsoleView(isShowConsoleView()).showExecutionsView(isShowExecutionsView());

            }
            BuildConfiguration configuration = configBuilder.build();
            this.context.log(ImportTaskMessages.GradleImportTaskImpl_import_new);
            return workspace.createBuild(configuration);
        }

    }

} // GradleImportTaskImpl
