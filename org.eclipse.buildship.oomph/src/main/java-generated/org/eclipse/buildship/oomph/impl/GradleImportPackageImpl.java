/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph.impl;

import org.eclipse.buildship.oomph.DistributionType;
import org.eclipse.buildship.oomph.GradleImportFactory;
import org.eclipse.buildship.oomph.GradleImportPackage;
import org.eclipse.buildship.oomph.GradleImportTask;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.oomph.base.BasePackage;

import org.eclipse.oomph.predicates.PredicatesPackage;

import org.eclipse.oomph.resources.ResourcesPackage;

import org.eclipse.oomph.setup.SetupPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!-- end-user-doc -->
 * 
 * @generated
 */
public class GradleImportPackageImpl extends EPackageImpl implements GradleImportPackage {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final String copyright = "Copyright (c) 2019 the original author or authors.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n";

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass gradleImportTaskEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EEnum distributionTypeEEnum = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package package URI
     * value.
     * <p>
     * Note: the correct way to create the package is via the static factory method {@link #init
     * init()}, which also performs initialization of the package, or returns the registered
     * package, if one already exists. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see org.eclipse.buildship.oomph.GradleImportPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private GradleImportPackageImpl() {
        super(eNS_URI, GradleImportFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model, and for any others
     * upon which it depends.
     *
     * <p>
     * This method is used to initialize {@link GradleImportPackage#eINSTANCE} when that field is
     * accessed. Clients should not invoke it directly. Instead, they should simply access that
     * field to obtain the package. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static GradleImportPackage init() {
        if (isInited)
            return (GradleImportPackage) EPackage.Registry.INSTANCE.getEPackage(GradleImportPackage.eNS_URI);

        // Obtain or create and register package
        Object registeredGradleImportPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
        GradleImportPackageImpl theGradleImportPackage = registeredGradleImportPackage instanceof GradleImportPackageImpl ? (GradleImportPackageImpl) registeredGradleImportPackage
                : new GradleImportPackageImpl();

        isInited = true;

        // Initialize simple dependencies
        BasePackage.eINSTANCE.eClass();
        PredicatesPackage.eINSTANCE.eClass();
        ResourcesPackage.eINSTANCE.eClass();
        SetupPackage.eINSTANCE.eClass();

        // Create package meta-data objects
        theGradleImportPackage.createPackageContents();

        // Initialize created meta-data
        theGradleImportPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theGradleImportPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(GradleImportPackage.eNS_URI, theGradleImportPackage);
        return theGradleImportPackage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getGradleImportTask() {
        return gradleImportTaskEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getGradleImportTask_SourceLocators() {
        return (EReference) gradleImportTaskEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_OverrideWorkspaceSettings() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_DistributionType() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_LocalInstallationDirectory() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_RemoteDistributionLocation() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_SpecificGradleVersion() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_ProgramArguments() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(6);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_JvmArguments() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(7);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_GradleUserHome() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(8);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_JavaHome() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(9);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_OfflineMode() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(10);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_BuildScans() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(11);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_AutomaticProjectSynchronization() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(12);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_ShowConsoleView() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(13);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getGradleImportTask_ShowExecutionsView() {
        return (EAttribute) gradleImportTaskEClass.getEStructuralFeatures().get(14);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EEnum getDistributionType() {
        return distributionTypeEEnum;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public GradleImportFactory getGradleImportFactory() {
        return (GradleImportFactory) getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package. This method is guarded to have no affect on
     * any invocation but its first. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void createPackageContents() {
        if (isCreated)
            return;
        isCreated = true;

        // Create classes and their features
        gradleImportTaskEClass = createEClass(GRADLE_IMPORT_TASK);
        createEReference(gradleImportTaskEClass, GRADLE_IMPORT_TASK__SOURCE_LOCATORS);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__JVM_ARGUMENTS);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__GRADLE_USER_HOME);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__JAVA_HOME);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__OFFLINE_MODE);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__BUILD_SCANS);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW);
        createEAttribute(gradleImportTaskEClass, GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW);

        // Create enums
        distributionTypeEEnum = createEEnum(DISTRIBUTION_TYPE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model. This method is guarded to have
     * no affect on any invocation but its first. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized)
            return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Obtain other dependent packages
        SetupPackage theSetupPackage = (SetupPackage) EPackage.Registry.INSTANCE.getEPackage(SetupPackage.eNS_URI);
        ResourcesPackage theResourcesPackage = (ResourcesPackage) EPackage.Registry.INSTANCE.getEPackage(ResourcesPackage.eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        gradleImportTaskEClass.getESuperTypes().add(theSetupPackage.getSetupTask());

        // Initialize classes and features; add operations and parameters
        initEClass(gradleImportTaskEClass, GradleImportTask.class, "GradleImportTask", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getGradleImportTask_SourceLocators(), theResourcesPackage
                .getSourceLocator(), null, "sourceLocators", null, 1, -1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_OverrideWorkspaceSettings(), ecorePackage
                .getEBoolean(), "overrideWorkspaceSettings", "false", 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_DistributionType(), this
                .getDistributionType(), "distributionType", "GRADLE_WRAPPER", 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_LocalInstallationDirectory(), ecorePackage
                .getEString(), "localInstallationDirectory", null, 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_RemoteDistributionLocation(), ecorePackage
                .getEString(), "remoteDistributionLocation", null, 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_SpecificGradleVersion(), ecorePackage
                .getEString(), "specificGradleVersion", null, 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_ProgramArguments(), ecorePackage
                .getEString(), "programArguments", null, 0, -1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_JvmArguments(), ecorePackage
                .getEString(), "jvmArguments", null, 0, -1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, !IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_GradleUserHome(), ecorePackage
                .getEString(), "gradleUserHome", null, 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_JavaHome(), ecorePackage
                .getEString(), "javaHome", null, 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_OfflineMode(), ecorePackage
                .getEBoolean(), "offlineMode", "false", 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_BuildScans(), ecorePackage
                .getEBoolean(), "buildScans", "false", 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_AutomaticProjectSynchronization(), ecorePackage
                .getEBoolean(), "automaticProjectSynchronization", "false", 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_ShowConsoleView(), ecorePackage
                .getEBoolean(), "showConsoleView", "false", 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getGradleImportTask_ShowExecutionsView(), ecorePackage
                .getEBoolean(), "showExecutionsView", "false", 0, 1, GradleImportTask.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(distributionTypeEEnum, DistributionType.class, "DistributionType");
        addEEnumLiteral(distributionTypeEEnum, DistributionType.GRADLE_WRAPPER);
        addEEnumLiteral(distributionTypeEEnum, DistributionType.LOCAL_INSTALLATION);
        addEEnumLiteral(distributionTypeEEnum, DistributionType.REMOTE_DISTRIBUTION);
        addEEnumLiteral(distributionTypeEEnum, DistributionType.SPECIFIC_GRADLE_VERSION);

        // Create resource
        createResource("https://raw.githubusercontent.com/eclipse/buildship/master/org.eclipse.buildship.oomph/model/GradleImport-1.0.ecore");

        // Create annotations
        // http://www.eclipse.org/emf/2002/Ecore
        createEcoreAnnotations();
        // http://www.eclipse.org/oomph/setup/ValidTriggers
        createValidTriggersAnnotations();
        // http://www.eclipse.org/oomph/setup/Enablement
        createEnablementAnnotations();
        // http:///org/eclipse/emf/ecore/util/ExtendedMetaData
        createExtendedMetaDataAnnotations();
    }

    /**
     * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/Ecore</b>. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void createEcoreAnnotations() {
        String source = "http://www.eclipse.org/emf/2002/Ecore";
        addAnnotation(this, source, new String[] { "schemaLocation",
                "https://raw.githubusercontent.com/eclipse/buildship/master/org.eclipse.buildship.oomph/model/GradleImport-1.0.ecore" });
    }

    /**
     * Initializes the annotations for <b>http://www.eclipse.org/oomph/setup/ValidTriggers</b>. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void createValidTriggersAnnotations() {
        String source = "http://www.eclipse.org/oomph/setup/ValidTriggers";
        addAnnotation(gradleImportTaskEClass, source, new String[] { "triggers", "STARTUP MANUAL" });
    }

    /**
     * Initializes the annotations for <b>http://www.eclipse.org/oomph/setup/Enablement</b>. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void createEnablementAnnotations() {
        String source = "http://www.eclipse.org/oomph/setup/Enablement";
        addAnnotation(gradleImportTaskEClass, source, new String[] { "variableName", "setup.buildship.p2", "repository", "https://download.eclipse.org/buildship/updates/latest",
                "installableUnits", "org.eclipse.buildship.feature.group" });
        addAnnotation(gradleImportTaskEClass, source, new String[] { "variableName", "setup.buildship.oomph.p2", "repository",
                "https://download.eclipse.org/buildship/updates/latest", "installableUnits", "org.eclipse.buildship.oomph.feature.group" });
    }

    /**
     * Initializes the annotations for <b>http:///org/eclipse/emf/ecore/util/ExtendedMetaData</b>.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void createExtendedMetaDataAnnotations() {
        String source = "http:///org/eclipse/emf/ecore/util/ExtendedMetaData";
        addAnnotation(getGradleImportTask_SourceLocators(), source, new String[] { "name", "sourceLocator" });
    }

} // GradleImportPackageImpl
