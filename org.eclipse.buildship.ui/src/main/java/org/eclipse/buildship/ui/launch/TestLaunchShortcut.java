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

import java.util.List;
import java.util.Map;

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
import org.eclipse.buildship.core.launch.RunGradleJvmTestMethodLaunchRequestJob;

/**
 * Runs tests from the editor or from the package explorer.
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
        // try to launch test methods
        List<IMethod> methodsToLaunch = resolver.resolveMethods();
        if (TestlaunchShortcutValidator.validateElements(methodsToLaunch)) {
            launchMethods(methodsToLaunch);
        } else {
            // If no test methods then try to launch test classes
            List<IType> typesToLaunch = resolver.resolveTypes();
            if (TestlaunchShortcutValidator.validateElements(typesToLaunch)) {
                launchClasses(typesToLaunch);
            } else {
                // if no classes/methods, then show a dialog
                showNoTestsFoundDialog();
            }
        }
    }

    private void launchMethods(List<IMethod> methods) {
        Map<String, Iterable<String>> methodNames = JavaElementNameCollector.collectClassNamesWithMethods(methods);
        IProject project = methods.get(0).getJavaProject().getProject();
        GradleRunConfigurationAttributes runConfigurationAttributes = collectRunConfigurationAttributes(project);
        new RunGradleJvmTestMethodLaunchRequestJob(methodNames, runConfigurationAttributes).schedule();
    }

    private void launchClasses(List<IType> classes) {
        Iterable<String> typeNames = JavaElementNameCollector.collectClassNames(classes);
        IProject project = classes.get(0).getJavaProject().getProject();
        GradleRunConfigurationAttributes runConfigurationAttributes = collectRunConfigurationAttributes(project);
        new RunGradleJvmTestLaunchRequestJob(typeNames, runConfigurationAttributes).schedule();
    }

    private GradleRunConfigurationAttributes collectRunConfigurationAttributes(IProject project) {
        FixedRequestAttributes attributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(project).getRequestAttributes();

        String projectDir = attributes.getProjectDir().getAbsolutePath();
        GradleDistribution gradleDistribution = attributes.getGradleDistribution();
        String gradleUserHome = attributes.getGradleUserHome() != null ? attributes.getGradleUserHome().getAbsolutePath() : null;
        String javaHome = attributes.getJavaHome() != null ? attributes.getJavaHome().getAbsolutePath() : null;
        List<String> jvmArguments = attributes.getJvmArguments();
        List<String> arguments = attributes.getArguments();
        boolean showExecutionView = true;
        boolean showConsoleView = false;

        return GradleRunConfigurationAttributes
                .with(ImmutableList.<String>of(), projectDir, gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, showExecutionView, showConsoleView);
    }

    private void showNoTestsFoundDialog() {
        CorePlugin.userNotification()
                .errorOccurred(LaunchMessages.Test_Not_Found_Dialog_Title, LaunchMessages.Test_Not_Found_Dialog_Message, LaunchMessages.Test_Not_Found_Dialog_Details, IStatus.WARNING, null);
    }
}
