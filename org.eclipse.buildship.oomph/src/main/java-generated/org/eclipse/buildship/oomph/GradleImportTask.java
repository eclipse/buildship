/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph;

import org.eclipse.emf.common.util.EList;

import org.eclipse.oomph.resources.SourceLocator;

import org.eclipse.oomph.setup.SetupTask;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Task</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getSourceLocators <em>Source
 * Locators</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#isOverrideWorkspaceSettings <em>Override
 * Workspace Settings</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getDistributionType <em>Distribution
 * Type</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getLocalInstallationDirectory <em>Local
 * Installation Directory</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getRemoteDistributionLocation <em>Remote
 * Distribution Location</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getSpecificGradleVersion <em>Specific
 * Gradle Version</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getProgramArguments <em>Program
 * Arguments</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getJvmArguments <em>Jvm
 * Arguments</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getGradleUserHome <em>Gradle User
 * Home</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#getJavaHome <em>Java Home</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#isOfflineMode <em>Offline Mode</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#isBuildScans <em>Build Scans</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#isAutomaticProjectSynchronization
 * <em>Automatic Project Synchronization</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#isShowConsoleView <em>Show Console
 * View</em>}</li>
 * <li>{@link org.eclipse.buildship.oomph.GradleImportTask#isShowExecutionsView <em>Show Executions
 * View</em>}</li>
 * </ul>
 *
 * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask()
 * @model annotation="http://www.eclipse.org/oomph/setup/ValidTriggers triggers='STARTUP MANUAL'"
 *        annotation="http://www.eclipse.org/oomph/setup/Enablement
 *        variableName='setup.buildship.p2'
 *        repository='https://download.eclipse.org/buildship/updates/latest'
 *        installableUnits='org.eclipse.buildship.feature.group'"
 *        annotation="http://www.eclipse.org/oomph/setup/Enablement
 *        variableName='setup.buildship.oomph.p2'
 *        repository='https://download.eclipse.org/buildship/updates/latest'
 *        installableUnits='org.eclipse.buildship.oomph.feature.group'"
 * @generated
 */
public interface GradleImportTask extends SetupTask {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String copyright = "Copyright (c) 2023 the original author or authors.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n";

    /**
     * Returns the value of the '<em><b>Source Locators</b></em>' containment reference list. The
     * list contents are of type {@link org.eclipse.oomph.resources.SourceLocator}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Source Locators</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Source Locators</em>' containment reference list.
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_SourceLocators()
     * @model containment="true" required="true" extendedMetaData="name='sourceLocator'"
     * @generated
     */
    EList<SourceLocator> getSourceLocators();

    /**
     * Returns the value of the '<em><b>Override Workspace Settings</b></em>' attribute. The default
     * value is <code>"false"</code>. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Override Workspace Settings</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Override Workspace Settings</em>' attribute.
     * @see #setOverrideWorkspaceSettings(boolean)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_OverrideWorkspaceSettings()
     * @model default="false"
     * @generated
     */
    boolean isOverrideWorkspaceSettings();

    /**
     * Sets the value of the
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isOverrideWorkspaceSettings <em>Override
     * Workspace Settings</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Override Workspace Settings</em>' attribute.
     * @see #isOverrideWorkspaceSettings()
     * @generated
     */
    void setOverrideWorkspaceSettings(boolean value);

    /**
     * Returns the value of the '<em><b>Distribution Type</b></em>' attribute. The default value is
     * <code>"GRADLE_WRAPPER"</code>. The literals are from the enumeration
     * {@link org.eclipse.buildship.oomph.DistributionType}. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Distribution Type</em>' attribute isn't clear, there really should
     * be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Distribution Type</em>' attribute.
     * @see org.eclipse.buildship.oomph.DistributionType
     * @see #setDistributionType(DistributionType)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_DistributionType()
     * @model default="GRADLE_WRAPPER"
     * @generated
     */
    DistributionType getDistributionType();

    /**
     * Sets the value of the
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getDistributionType <em>Distribution
     * Type</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Distribution Type</em>' attribute.
     * @see org.eclipse.buildship.oomph.DistributionType
     * @see #getDistributionType()
     * @generated
     */
    void setDistributionType(DistributionType value);

    /**
     * Returns the value of the '<em><b>Local Installation Directory</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Local Installation Directory</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Local Installation Directory</em>' attribute.
     * @see #setLocalInstallationDirectory(String)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_LocalInstallationDirectory()
     * @model
     * @generated
     */
    String getLocalInstallationDirectory();

    /**
     * Sets the value of the
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getLocalInstallationDirectory <em>Local
     * Installation Directory</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Local Installation Directory</em>' attribute.
     * @see #getLocalInstallationDirectory()
     * @generated
     */
    void setLocalInstallationDirectory(String value);

    /**
     * Returns the value of the '<em><b>Remote Distribution Location</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Remote Distribution Location</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Remote Distribution Location</em>' attribute.
     * @see #setRemoteDistributionLocation(String)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_RemoteDistributionLocation()
     * @model
     * @generated
     */
    String getRemoteDistributionLocation();

    /**
     * Sets the value of the
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getRemoteDistributionLocation <em>Remote
     * Distribution Location</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Remote Distribution Location</em>' attribute.
     * @see #getRemoteDistributionLocation()
     * @generated
     */
    void setRemoteDistributionLocation(String value);

    /**
     * Returns the value of the '<em><b>Specific Gradle Version</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Specific Gradle Version</em>' attribute isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Specific Gradle Version</em>' attribute.
     * @see #setSpecificGradleVersion(String)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_SpecificGradleVersion()
     * @model
     * @generated
     */
    String getSpecificGradleVersion();

    /**
     * Sets the value of the
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#getSpecificGradleVersion <em>Specific
     * Gradle Version</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Specific Gradle Version</em>' attribute.
     * @see #getSpecificGradleVersion()
     * @generated
     */
    void setSpecificGradleVersion(String value);

    /**
     * Returns the value of the '<em><b>Program Arguments</b></em>' attribute list. The list
     * contents are of type {@link java.lang.String}. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Program Arguments</em>' attribute list isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Program Arguments</em>' attribute list.
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_ProgramArguments()
     * @model unique="false"
     * @generated
     */
    EList<String> getProgramArguments();

    /**
     * Returns the value of the '<em><b>Jvm Arguments</b></em>' attribute list. The list contents
     * are of type {@link java.lang.String}. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Jvm Arguments</em>' attribute list isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Jvm Arguments</em>' attribute list.
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_JvmArguments()
     * @model unique="false"
     * @generated
     */
    EList<String> getJvmArguments();

    /**
     * Returns the value of the '<em><b>Gradle User Home</b></em>' attribute. <!-- begin-user-doc
     * -->
     * <p>
     * If the meaning of the '<em>Gradle User Home</em>' attribute isn't clear, there really should
     * be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Gradle User Home</em>' attribute.
     * @see #setGradleUserHome(String)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_GradleUserHome()
     * @model
     * @generated
     */
    String getGradleUserHome();

    /**
     * Sets the value of the '{@link org.eclipse.buildship.oomph.GradleImportTask#getGradleUserHome
     * <em>Gradle User Home</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Gradle User Home</em>' attribute.
     * @see #getGradleUserHome()
     * @generated
     */
    void setGradleUserHome(String value);

    /**
     * Returns the value of the '<em><b>Java Home</b></em>' attribute. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Java Home</em>' attribute isn't clear, there really should be more
     * of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Java Home</em>' attribute.
     * @see #setJavaHome(String)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_JavaHome()
     * @model
     * @generated
     */
    String getJavaHome();

    /**
     * Sets the value of the '{@link org.eclipse.buildship.oomph.GradleImportTask#getJavaHome
     * <em>Java Home</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Java Home</em>' attribute.
     * @see #getJavaHome()
     * @generated
     */
    void setJavaHome(String value);

    /**
     * Returns the value of the '<em><b>Offline Mode</b></em>' attribute. The default value is
     * <code>"false"</code>. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Offline Mode</em>' attribute isn't clear, there really should be
     * more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Offline Mode</em>' attribute.
     * @see #setOfflineMode(boolean)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_OfflineMode()
     * @model default="false"
     * @generated
     */
    boolean isOfflineMode();

    /**
     * Sets the value of the '{@link org.eclipse.buildship.oomph.GradleImportTask#isOfflineMode
     * <em>Offline Mode</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Offline Mode</em>' attribute.
     * @see #isOfflineMode()
     * @generated
     */
    void setOfflineMode(boolean value);

    /**
     * Returns the value of the '<em><b>Build Scans</b></em>' attribute. The default value is
     * <code>"false"</code>. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Build Scans</em>' attribute isn't clear, there really should be
     * more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Build Scans</em>' attribute.
     * @see #setBuildScans(boolean)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_BuildScans()
     * @model default="false"
     * @generated
     */
    boolean isBuildScans();

    /**
     * Sets the value of the '{@link org.eclipse.buildship.oomph.GradleImportTask#isBuildScans
     * <em>Build Scans</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Build Scans</em>' attribute.
     * @see #isBuildScans()
     * @generated
     */
    void setBuildScans(boolean value);

    /**
     * Returns the value of the '<em><b>Automatic Project Synchronization</b></em>' attribute. The
     * default value is <code>"false"</code>. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Automatic Project Synchronization</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Automatic Project Synchronization</em>' attribute.
     * @see #setAutomaticProjectSynchronization(boolean)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_AutomaticProjectSynchronization()
     * @model default="false"
     * @generated
     */
    boolean isAutomaticProjectSynchronization();

    /**
     * Sets the value of the
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isAutomaticProjectSynchronization
     * <em>Automatic Project Synchronization</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value the new value of the '<em>Automatic Project Synchronization</em>' attribute.
     * @see #isAutomaticProjectSynchronization()
     * @generated
     */
    void setAutomaticProjectSynchronization(boolean value);

    /**
     * Returns the value of the '<em><b>Show Console View</b></em>' attribute. The default value is
     * <code>"false"</code>. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Show Console View</em>' attribute isn't clear, there really should
     * be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Show Console View</em>' attribute.
     * @see #setShowConsoleView(boolean)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_ShowConsoleView()
     * @model default="false"
     * @generated
     */
    boolean isShowConsoleView();

    /**
     * Sets the value of the '{@link org.eclipse.buildship.oomph.GradleImportTask#isShowConsoleView
     * <em>Show Console View</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Show Console View</em>' attribute.
     * @see #isShowConsoleView()
     * @generated
     */
    void setShowConsoleView(boolean value);

    /**
     * Returns the value of the '<em><b>Show Executions View</b></em>' attribute. The default value
     * is <code>"false"</code>. <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Show Executions View</em>' attribute isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Show Executions View</em>' attribute.
     * @see #setShowExecutionsView(boolean)
     * @see org.eclipse.buildship.oomph.GradleImportPackage#getGradleImportTask_ShowExecutionsView()
     * @model default="false"
     * @generated
     */
    boolean isShowExecutionsView();

    /**
     * Sets the value of the
     * '{@link org.eclipse.buildship.oomph.GradleImportTask#isShowExecutionsView <em>Show Executions
     * View</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>Show Executions View</em>' attribute.
     * @see #isShowExecutionsView()
     * @generated
     */
    void setShowExecutionsView(boolean value);

} // GradleImportTask
