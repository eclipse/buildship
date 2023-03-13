/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.oomph.setup.SetupPackage;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains accessors for the meta
 * objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see org.eclipse.buildship.oomph.GradleImportFactory
 * @model kind="package" annotation="http://www.eclipse.org/emf/2002/Ecore
 *        schemaLocation='https://raw.githubusercontent.com/eclipse/buildship/master/org.eclipse.buildship.oomph/model/GradleImport-1.0.ecore'"
 * @generated
 */
public interface GradleImportPackage extends EPackage {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String copyright = "Copyright (c) 2019 the original author or authors.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n";

    /**
     * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String eNAME = "oomph";

    /**
     * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String eNS_URI = "http://www.eclipse.org/buildship/oomph/1.0";

    /**
     * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String eNS_PREFIX = "oomph";

    /**
     * The singleton instance of the package. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    GradleImportPackage eINSTANCE = org.eclipse.buildship.oomph.impl.GradleImportPackageImpl.init();

    /**
     * The meta object id for the '{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl
     * <em>Task</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see org.eclipse.buildship.oomph.impl.GradleImportTaskImpl
     * @see org.eclipse.buildship.oomph.impl.GradleImportPackageImpl#getGradleImportTask()
     * @generated
     */
    int GRADLE_IMPORT_TASK = 0;

    /**
     * The feature id for the '<em><b>Annotations</b></em>' containment reference list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__ANNOTATIONS = SetupPackage.SETUP_TASK__ANNOTATIONS;

    /**
     * The feature id for the '<em><b>ID</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__ID = SetupPackage.SETUP_TASK__ID;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__DESCRIPTION = SetupPackage.SETUP_TASK__DESCRIPTION;

    /**
     * The feature id for the '<em><b>Scope Type</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__SCOPE_TYPE = SetupPackage.SETUP_TASK__SCOPE_TYPE;

    /**
     * The feature id for the '<em><b>Excluded Triggers</b></em>' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__EXCLUDED_TRIGGERS = SetupPackage.SETUP_TASK__EXCLUDED_TRIGGERS;

    /**
     * The feature id for the '<em><b>Disabled</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__DISABLED = SetupPackage.SETUP_TASK__DISABLED;

    /**
     * The feature id for the '<em><b>Predecessors</b></em>' reference list. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__PREDECESSORS = SetupPackage.SETUP_TASK__PREDECESSORS;

    /**
     * The feature id for the '<em><b>Successors</b></em>' reference list. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__SUCCESSORS = SetupPackage.SETUP_TASK__SUCCESSORS;

    /**
     * The feature id for the '<em><b>Restrictions</b></em>' reference list. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__RESTRICTIONS = SetupPackage.SETUP_TASK__RESTRICTIONS;

    /**
     * The feature id for the '<em><b>Filter</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__FILTER = SetupPackage.SETUP_TASK__FILTER;

    /**
     * The feature id for the '<em><b>Source Locators</b></em>' containment reference list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__SOURCE_LOCATORS = SetupPackage.SETUP_TASK_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Override Workspace Settings</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS = SetupPackage.SETUP_TASK_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Distribution Type</b></em>' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE = SetupPackage.SETUP_TASK_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Local Installation Directory</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY = SetupPackage.SETUP_TASK_FEATURE_COUNT + 3;

    /**
     * The feature id for the '<em><b>Remote Distribution Location</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION = SetupPackage.SETUP_TASK_FEATURE_COUNT + 4;

    /**
     * The feature id for the '<em><b>Specific Gradle Version</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION = SetupPackage.SETUP_TASK_FEATURE_COUNT + 5;

    /**
     * The feature id for the '<em><b>Program Arguments</b></em>' attribute list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS = SetupPackage.SETUP_TASK_FEATURE_COUNT + 6;

    /**
     * The feature id for the '<em><b>Jvm Arguments</b></em>' attribute list. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__JVM_ARGUMENTS = SetupPackage.SETUP_TASK_FEATURE_COUNT + 7;

    /**
     * The feature id for the '<em><b>Gradle User Home</b></em>' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__GRADLE_USER_HOME = SetupPackage.SETUP_TASK_FEATURE_COUNT + 8;

    /**
     * The feature id for the '<em><b>Java Home</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__JAVA_HOME = SetupPackage.SETUP_TASK_FEATURE_COUNT + 9;

    /**
     * The feature id for the '<em><b>Offline Mode</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__OFFLINE_MODE = SetupPackage.SETUP_TASK_FEATURE_COUNT + 10;

    /**
     * The feature id for the '<em><b>Build Scans</b></em>' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__BUILD_SCANS = SetupPackage.SETUP_TASK_FEATURE_COUNT + 11;

    /**
     * The feature id for the '<em><b>Automatic Project Synchronization</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION = SetupPackage.SETUP_TASK_FEATURE_COUNT + 12;

    /**
     * The feature id for the '<em><b>Show Console View</b></em>' attribute. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW = SetupPackage.SETUP_TASK_FEATURE_COUNT + 13;

    /**
     * The feature id for the '<em><b>Show Executions View</b></em>' attribute. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW = SetupPackage.SETUP_TASK_FEATURE_COUNT + 14;

    /**
     * The number of structural features of the '<em>Task</em>' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int GRADLE_IMPORT_TASK_FEATURE_COUNT = SetupPackage.SETUP_TASK_FEATURE_COUNT + 15;

    /**
     * The meta object id for the '{@link org.eclipse.buildship.oomph.DistributionType
     * <em>Distribution Type</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see org.eclipse.buildship.oomph.DistributionType
     * @see org.eclipse.buildship.oomph.impl.GradleImportPackageImpl#getDistributionType()
     * @generated
     */
    int DISTRIBUTION_TYPE = 1;

    /**
     * Returns the meta object for class '{@link org.eclipse.buildship.oomph.GradleImportTask
     * <em>Task</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Task</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask
     * @generated
     */
    EClass getGradleImportTask();

    /**
     * Returns the meta object for the containment reference list
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getSourceLocators <em>Source
     * Locators</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Source Locators</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getSourceLocators()
     * @see #getGradleImportTask()
     * @generated
     */
    EReference getGradleImportTask_SourceLocators();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isOverrideWorkspaceSettings <em>Override
     * Workspace Settings</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Override Workspace Settings</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#isOverrideWorkspaceSettings()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_OverrideWorkspaceSettings();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getDistributionType <em>Distribution
     * Type</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Distribution Type</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getDistributionType()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_DistributionType();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getLocalInstallationDirectory <em>Local
     * Installation Directory</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Local Installation Directory</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getLocalInstallationDirectory()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_LocalInstallationDirectory();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getRemoteDistributionLocation <em>Remote
     * Distribution Location</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Remote Distribution Location</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getRemoteDistributionLocation()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_RemoteDistributionLocation();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getSpecificGradleVersion <em>Specific
     * Gradle Version</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Specific Gradle Version</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getSpecificGradleVersion()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_SpecificGradleVersion();

    /**
     * Returns the meta object for the attribute list
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getProgramArguments <em>Program
     * Arguments</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute list '<em>Program Arguments</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getProgramArguments()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_ProgramArguments();

    /**
     * Returns the meta object for the attribute list
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getJvmArguments <em>Jvm
     * Arguments</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute list '<em>Jvm Arguments</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getJvmArguments()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_JvmArguments();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getGradleUserHome <em>Gradle User
     * Home</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Gradle User Home</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getGradleUserHome()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_GradleUserHome();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getJavaHome <em>Java Home</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Java Home</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#getJavaHome()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_JavaHome();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isOfflineMode <em>Offline Mode</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Offline Mode</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#isOfflineMode()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_OfflineMode();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isBuildScans <em>Build Scans</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Build Scans</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#isBuildScans()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_BuildScans();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isAutomaticProjectSynchronization
     * <em>Automatic Project Synchronization</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Automatic Project Synchronization</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#isAutomaticProjectSynchronization()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_AutomaticProjectSynchronization();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isShowConsoleView <em>Show Console
     * View</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Show Console View</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#isShowConsoleView()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_ShowConsoleView();

    /**
     * Returns the meta object for the attribute
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isShowExecutionsView <em>Show Executions
     * View</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Show Executions View</em>'.
     * @see org.eclipse.buildship.oomph.GradleImportTask#isShowExecutionsView()
     * @see #getGradleImportTask()
     * @generated
     */
    EAttribute getGradleImportTask_ShowExecutionsView();

    /**
     * Returns the meta object for enum '{@link org.eclipse.buildship.oomph.DistributionType
     * <em>Distribution Type</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for enum '<em>Distribution Type</em>'.
     * @see org.eclipse.buildship.oomph.DistributionType
     * @generated
     */
    EEnum getDistributionType();

    /**
     * Returns the factory that creates the instances of the model. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @return the factory that creates the instances of the model.
     * @generated
     */
    GradleImportFactory getGradleImportFactory();

    /**
     * <!-- begin-user-doc --> Defines literals for the meta objects that represent
     * <ul>
     * <li>each class,</li>
     * <li>each feature of each class,</li>
     * <li>each enum,</li>
     * <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    interface Literals {

        /**
         * The meta object literal for the
         * '{@link org.eclipse.buildship.oomph.impl.GradleImportTaskImpl <em>Task</em>}' class. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see org.eclipse.buildship.oomph.impl.GradleImportTaskImpl
         * @see org.eclipse.buildship.oomph.impl.GradleImportPackageImpl#getGradleImportTask()
         * @generated
         */
        EClass GRADLE_IMPORT_TASK = eINSTANCE.getGradleImportTask();

        /**
         * The meta object literal for the '<em><b>Source Locators</b></em>' containment reference
         * list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference GRADLE_IMPORT_TASK__SOURCE_LOCATORS = eINSTANCE.getGradleImportTask_SourceLocators();

        /**
         * The meta object literal for the '<em><b>Override Workspace Settings</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS = eINSTANCE.getGradleImportTask_OverrideWorkspaceSettings();

        /**
         * The meta object literal for the '<em><b>Distribution Type</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE = eINSTANCE.getGradleImportTask_DistributionType();

        /**
         * The meta object literal for the '<em><b>Local Installation Directory</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY = eINSTANCE.getGradleImportTask_LocalInstallationDirectory();

        /**
         * The meta object literal for the '<em><b>Remote Distribution Location</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION = eINSTANCE.getGradleImportTask_RemoteDistributionLocation();

        /**
         * The meta object literal for the '<em><b>Specific Gradle Version</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION = eINSTANCE.getGradleImportTask_SpecificGradleVersion();

        /**
         * The meta object literal for the '<em><b>Program Arguments</b></em>' attribute list
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS = eINSTANCE.getGradleImportTask_ProgramArguments();

        /**
         * The meta object literal for the '<em><b>Jvm Arguments</b></em>' attribute list feature.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__JVM_ARGUMENTS = eINSTANCE.getGradleImportTask_JvmArguments();

        /**
         * The meta object literal for the '<em><b>Gradle User Home</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__GRADLE_USER_HOME = eINSTANCE.getGradleImportTask_GradleUserHome();

        /**
         * The meta object literal for the '<em><b>Java Home</b></em>' attribute feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__JAVA_HOME = eINSTANCE.getGradleImportTask_JavaHome();

        /**
         * The meta object literal for the '<em><b>Offline Mode</b></em>' attribute feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__OFFLINE_MODE = eINSTANCE.getGradleImportTask_OfflineMode();

        /**
         * The meta object literal for the '<em><b>Build Scans</b></em>' attribute feature. <!--
         * begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__BUILD_SCANS = eINSTANCE.getGradleImportTask_BuildScans();

        /**
         * The meta object literal for the '<em><b>Automatic Project Synchronization</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION = eINSTANCE.getGradleImportTask_AutomaticProjectSynchronization();

        /**
         * The meta object literal for the '<em><b>Show Console View</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW = eINSTANCE.getGradleImportTask_ShowConsoleView();

        /**
         * The meta object literal for the '<em><b>Show Executions View</b></em>' attribute feature.
         * <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW = eINSTANCE.getGradleImportTask_ShowExecutionsView();

        /**
         * The meta object literal for the '{@link org.eclipse.buildship.oomph.DistributionType
         * <em>Distribution Type</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see org.eclipse.buildship.oomph.DistributionType
         * @see org.eclipse.buildship.oomph.impl.GradleImportPackageImpl#getDistributionType()
         * @generated
         */
        EEnum DISTRIBUTION_TYPE = eINSTANCE.getDistributionType();

    }

} // GradleImportPackage
