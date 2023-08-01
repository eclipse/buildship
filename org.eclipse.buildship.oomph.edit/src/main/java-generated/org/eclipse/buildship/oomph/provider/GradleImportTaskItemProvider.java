/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.oomph.provider;

import java.util.Collection;
import java.util.List;

import org.eclipse.buildship.oomph.GradleImportPackage;
import org.eclipse.buildship.oomph.GradleImportTask;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

import org.eclipse.oomph.resources.ResourcesFactory;

import org.eclipse.oomph.setup.provider.SetupTaskItemProvider;

/**
 * This is the item provider adapter for a {@link org.eclipse.buildship.oomph.GradleImportTask}
 * object. <!-- begin-user-doc --> <!-- end-user-doc -->
 * 
 * @generated
 */
public class GradleImportTaskItemProvider extends SetupTaskItemProvider {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final String copyright = "Copyright (c) 2023 the original author or authors.\nAll rights reserved. This program and the accompanying materials\nare made available under the terms of the Eclipse Public License v1.0\nwhich accompanies this distribution, and is available at\nhttp://www.eclipse.org/legal/epl-v10.html\n";

    /**
     * This constructs an instance from a factory and a notifier. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public GradleImportTaskItemProvider(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    /**
     * This returns the property descriptors for the adapted class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    @Override
    public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
        if (itemPropertyDescriptors == null) {
            super.getPropertyDescriptors(object);

            addOverrideWorkspaceSettingsPropertyDescriptor(object);
            addDistributionTypePropertyDescriptor(object);
            addLocalInstallationDirectoryPropertyDescriptor(object);
            addRemoteDistributionLocationPropertyDescriptor(object);
            addSpecificGradleVersionPropertyDescriptor(object);
            addProgramArgumentsPropertyDescriptor(object);
            addJvmArgumentsPropertyDescriptor(object);
            addGradleUserHomePropertyDescriptor(object);
            addJavaHomePropertyDescriptor(object);
            addOfflineModePropertyDescriptor(object);
            addBuildScansPropertyDescriptor(object);
            addAutomaticProjectSynchronizationPropertyDescriptor(object);
            addShowConsoleViewPropertyDescriptor(object);
            addShowExecutionsViewPropertyDescriptor(object);
        }
        return itemPropertyDescriptors;
    }

    /**
     * This adds a property descriptor for the Override Workspace Settings feature. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected void addOverrideWorkspaceSettingsPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_overrideWorkspaceSettings_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_overrideWorkspaceSettings_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Distribution Type feature. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void addDistributionTypePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_distributionType_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_distributionType_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Local Installation Directory feature. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected void addLocalInstallationDirectoryPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_localInstallationDirectory_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_localInstallationDirectory_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Remote Distribution Location feature. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected void addRemoteDistributionLocationPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_remoteDistributionLocation_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_remoteDistributionLocation_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Specific Gradle Version feature. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void addSpecificGradleVersionPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_specificGradleVersion_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_specificGradleVersion_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Program Arguments feature. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void addProgramArgumentsPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_programArguments_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_programArguments_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Jvm Arguments feature. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected void addJvmArgumentsPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_jvmArguments_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_jvmArguments_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__JVM_ARGUMENTS, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Gradle User Home feature. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void addGradleUserHomePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_gradleUserHome_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_gradleUserHome_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__GRADLE_USER_HOME, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Java Home feature. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected void addJavaHomePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_javaHome_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_javaHome_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__JAVA_HOME, true, false, false, ItemPropertyDescriptor.GENERIC_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Offline Mode feature. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected void addOfflineModePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_offlineMode_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_offlineMode_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__OFFLINE_MODE, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Build Scans feature. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    protected void addBuildScansPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_buildScans_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_buildScans_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__BUILD_SCANS, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Automatic Project Synchronization feature. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected void addAutomaticProjectSynchronizationPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_automaticProjectSynchronization_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_automaticProjectSynchronization_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Show Console View feature. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void addShowConsoleViewPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_showConsoleView_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_showConsoleView_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
    }

    /**
     * This adds a property descriptor for the Show Executions View feature. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    protected void addShowExecutionsViewPropertyDescriptor(Object object) {
        itemPropertyDescriptors.add(createItemPropertyDescriptor(((ComposeableAdapterFactory) adapterFactory)
                .getRootAdapterFactory(), getResourceLocator(), getString("_UI_GradleImportTask_showExecutionsView_feature"), getString("_UI_PropertyDescriptor_description", "_UI_GradleImportTask_showExecutionsView_feature", "_UI_GradleImportTask_type"), GradleImportPackage.Literals.GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW, true, false, false, ItemPropertyDescriptor.BOOLEAN_VALUE_IMAGE, null, null));
    }

    /**
     * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate
     * feature for an {@link org.eclipse.emf.edit.command.AddCommand},
     * {@link org.eclipse.emf.edit.command.RemoveCommand} or
     * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
        if (childrenFeatures == null) {
            super.getChildrenFeatures(object);
            childrenFeatures.add(GradleImportPackage.Literals.GRADLE_IMPORT_TASK__SOURCE_LOCATORS);
        }
        return childrenFeatures;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EStructuralFeature getChildFeature(Object object, Object child) {
        // Check the type of the specified child object and return the proper feature to use for
        // adding (see {@link AddCommand}) it as a child.

        return super.getChildFeature(object, child);
    }

    /**
     * This returns gradle_file.png.
     *
     * @generated NOT
     */
    @Override
    public Object getImage(Object object) {
        return overlayImage(object, getResourceLocator().getImage("full/obj16/gradle_file.png"));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected boolean shouldComposeCreationImage() {
        return true;
    }

    /**
     * This returns the label text for the adapted class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @generated
     */
    @Override
    public String getText(Object object) {
        String label = ((GradleImportTask) object).getID();
        return label == null || label.length() == 0 ? getString("_UI_GradleImportTask_type") : getString("_UI_GradleImportTask_type") + " " + label;
    }

    /**
     * This handles model notifications by calling {@link #updateChildren} to update any cached
     * children and by creating a viewer notification, which it passes to
     * {@link #fireNotifyChanged}. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void notifyChanged(Notification notification) {
        updateChildren(notification);

        switch (notification.getFeatureID(GradleImportTask.class)) {
            case GradleImportPackage.GRADLE_IMPORT_TASK__OVERRIDE_WORKSPACE_SETTINGS:
            case GradleImportPackage.GRADLE_IMPORT_TASK__DISTRIBUTION_TYPE:
            case GradleImportPackage.GRADLE_IMPORT_TASK__LOCAL_INSTALLATION_DIRECTORY:
            case GradleImportPackage.GRADLE_IMPORT_TASK__REMOTE_DISTRIBUTION_LOCATION:
            case GradleImportPackage.GRADLE_IMPORT_TASK__SPECIFIC_GRADLE_VERSION:
            case GradleImportPackage.GRADLE_IMPORT_TASK__PROGRAM_ARGUMENTS:
            case GradleImportPackage.GRADLE_IMPORT_TASK__JVM_ARGUMENTS:
            case GradleImportPackage.GRADLE_IMPORT_TASK__GRADLE_USER_HOME:
            case GradleImportPackage.GRADLE_IMPORT_TASK__JAVA_HOME:
            case GradleImportPackage.GRADLE_IMPORT_TASK__OFFLINE_MODE:
            case GradleImportPackage.GRADLE_IMPORT_TASK__BUILD_SCANS:
            case GradleImportPackage.GRADLE_IMPORT_TASK__AUTOMATIC_PROJECT_SYNCHRONIZATION:
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_CONSOLE_VIEW:
            case GradleImportPackage.GRADLE_IMPORT_TASK__SHOW_EXECUTIONS_VIEW:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
                return;
            case GradleImportPackage.GRADLE_IMPORT_TASK__SOURCE_LOCATORS:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
                return;
        }
        super.notifyChanged(notification);
    }

    /**
     * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children that
     * can be created under this object. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
        super.collectNewChildDescriptors(newChildDescriptors, object);

        newChildDescriptors.add(createChildParameter(GradleImportPackage.Literals.GRADLE_IMPORT_TASK__SOURCE_LOCATORS, ResourcesFactory.eINSTANCE.createSourceLocator()));
    }

}
