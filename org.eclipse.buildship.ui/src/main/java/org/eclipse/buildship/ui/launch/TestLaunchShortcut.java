/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.launch;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingclient.TestConfig.Builder;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.launch.RunGradleJvmTestLaunchRequestJob;
import org.eclipse.buildship.core.launch.RunGradleJvmTestMethodLaunchRequestJob;
import org.eclipse.buildship.core.launch.TestTarget;
import org.eclipse.buildship.core.util.file.FileUtils;

/**
 * Shortcut for Gradle test launches from the Java editor or from the current selection.
 */
public final class TestLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        JavaElementResolver resolver = SelectionJavaElementResolver.from(selection);
        launch(resolver);
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        JavaElementResolver resolver = EditorJavaElementResolver.from(editor);
        launch(resolver);
    }

    private void launch(JavaElementResolver resolver) {
        // try to launch test methods, otherwise try to launch entire test classes
        List<IMethod> methods = resolver.resolveMethods();
        if (TestLaunchShortcutValidator.validateMethods(methods)) {
            launchMethods(methods);
        } else {
            List<IType> types = resolver.resolveTypes();
            if (TestLaunchShortcutValidator.validateTypes(types)) {
                launchClasses(types);
            } else {
                showNoTestsFoundDialog();
            }
        }
    }

    private void launchMethods(List<IMethod> methods) {
        IProject project = methods.get(0).getJavaProject().getProject();
        GradleRunConfigurationAttributes runConfigurationAttributes = collectRunConfigurationAttributes(project);
        new RunGradleJvmTestMethodLaunchRequestJob(methods, runConfigurationAttributes).schedule();
    }

    private void launchClasses(List<IType> types) {
        IProject project = types.get(0).getJavaProject().getProject();
        GradleRunConfigurationAttributes runConfigurationAttributes = collectRunConfigurationAttributes(project);
        new RunGradleJvmTestLaunchRequestJob(toTestTargets(types), runConfigurationAttributes).schedule();
    }

    @SuppressWarnings("ConstantConditions")
    private GradleRunConfigurationAttributes collectRunConfigurationAttributes(IProject project) {
        FixedRequestAttributes requestAttributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();

        // configure the project directory
        String projectDir = requestAttributes.getProjectDir().getAbsolutePath();

        // configure the advanced options
        GradleDistribution gradleDistribution = requestAttributes.getGradleDistribution();
        String gradleUserHome = FileUtils.getAbsolutePath(requestAttributes.getGradleUserHome()).orNull();
        String javaHome = FileUtils.getAbsolutePath(requestAttributes.getJavaHome()).orNull();
        List<String> jvmArguments = requestAttributes.getJvmArguments();
        List<String> arguments = requestAttributes.getArguments();

        // have execution view and console view enabled by default
        boolean showExecutionView = true;
        boolean showConsoleView = true;

        return GradleRunConfigurationAttributes.with(ImmutableList.<String>of(), projectDir, gradleDistribution, gradleUserHome,
                javaHome, jvmArguments, arguments, showExecutionView, showConsoleView);
    }

    private void showNoTestsFoundDialog() {
        CorePlugin.userNotification().errorOccurred(
                LaunchMessages.Test_Not_Found_Dialog_Title,
                LaunchMessages.Test_Not_Found_Dialog_Message,
                LaunchMessages.Test_Not_Found_Dialog_Details,
                IStatus.WARNING,
                null);
    }

    private static List<TestTarget> toTestTargets(Collection<? extends IType> types) {
        return FluentIterable.from(types).transform(new Function<IType, TestTarget>() {

            @Override
            public TestTarget apply(IType type) {
                return TestType.from(type);
            }
        }).toList();
    }

    /**
     * {@link TestTarget} implementation backed by an {@link IMethod} instance.
     */
    private static final class TestType implements TestTarget {

        private final IType type;

        private TestType(IType type) {
            this.type = Preconditions.checkNotNull(type);
        }

        @Override
        public String getSimpleName() {
            return type.getElementName();
        }

        @Override
        public String getQualifiedName() {
            return type.getFullyQualifiedName();
        }

        @Override
        public void apply(Builder testConfig) {
            testConfig.jvmTestClasses(type.getFullyQualifiedName());
        }

        public static TestType from(IType type) {
            return new TestType(type);
        }
    }

}
