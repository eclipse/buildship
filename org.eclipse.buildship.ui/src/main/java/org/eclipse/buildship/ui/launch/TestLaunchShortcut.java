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
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.GradleDistribution;
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
import org.eclipse.buildship.core.launch.TestMethod;
import org.eclipse.buildship.core.launch.TestTarget;
import org.eclipse.buildship.core.launch.TestType;
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
        ImmutableList.Builder<TestTarget> targets = ImmutableList.builder();
        
        List<IMethod> methods = resolver.resolveMethods();
        if (TestLaunchShortcutValidator.validateMethods(methods)) {
            targets.addAll(convertMethodsToTestTargets(methods));
        }

        List<IType> types = resolver.resolveTypes();
        if (TestLaunchShortcutValidator.validateTypes(types)) {
            targets.addAll(convertTypesToTestTargets(types));
        }

        List<TestTarget> testTargets = targets.build();
        if (!testTargets.isEmpty()) {
            IProject project = types.get(0).getJavaProject().getProject();
            GradleRunConfigurationAttributes runConfigurationAttributes = collectRunConfigurationAttributes(project);
            new RunGradleJvmTestLaunchRequestJob(testTargets, runConfigurationAttributes).schedule();
        } else {
            showNoTestsFoundDialog();
        }
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
        // TODO (donat) null is not allowed
        CorePlugin.userNotification().errorOccurred(
                LaunchMessages.Test_Not_Found_Dialog_Title,
                LaunchMessages.Test_Not_Found_Dialog_Message,
                LaunchMessages.Test_Not_Found_Dialog_Details,
                IStatus.WARNING,
                null);
    }

    private static List<TestTarget> convertTypesToTestTargets(Collection<IType> types) {
        return FluentIterable.from(types).transform(new Function<IType, TestTarget>() {

            @Override
            public TestTarget apply(IType type) {
                return TestType.from(type);
            }
        }).toList();
    }

    private static List<TestTarget> convertMethodsToTestTargets(Collection<IMethod> methods) {
        return FluentIterable.from(methods).transform(new Function<IMethod, TestTarget>() {

            @Override
            public TestTarget apply(IMethod method) {
                return TestMethod.from(method);
            }
        }).toList();
    }

}
