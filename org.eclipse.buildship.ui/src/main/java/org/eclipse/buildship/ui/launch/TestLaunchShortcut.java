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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.launch.*;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import java.util.Collection;
import java.util.List;

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
        List<IType> types = resolver.resolveTypes();
        List<IMethod> methods = resolver.resolveMethods();
        if (TestLaunchShortcutValidator.validateTypesAndMethods(types, methods)) {
            ImmutableList.Builder<TestTarget> targets = ImmutableList.builder();
            targets.addAll(convertTypesToTestTargets(types));
            targets.addAll(convertMethodsToTestTargets(methods));
            GradleRunConfigurationAttributes runConfigurationAttributes = collectRunConfigurationAttributes(resolver.findFirstContainerProject().get());
            new RunGradleJvmTestLaunchRequestJob(targets.build(), runConfigurationAttributes).schedule();
        } else {
            showNoTestsFoundDialog();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private GradleRunConfigurationAttributes collectRunConfigurationAttributes(IProject project) {
        FixedRequestAttributes requestAttributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(project).toRequestAttributes();

        // configure the project directory
        String projectDir = requestAttributes.getProjectDir().getAbsolutePath();

        // configure the advanced options
        GradleDistribution gradleDistribution = requestAttributes.getGradleDistribution();
        String javaHome = FileUtils.getAbsolutePath(requestAttributes.getJavaHome()).orNull();
        List<String> jvmArguments = requestAttributes.getJvmArguments();
        List<String> arguments = requestAttributes.getArguments();

        // have execution view and console view enabled by default
        boolean showExecutionView = true;
        boolean showConsoleView = true;

        return GradleRunConfigurationAttributes.with(ImmutableList.<String>of(), projectDir, gradleDistribution,
                javaHome, jvmArguments, arguments, showExecutionView, showConsoleView, true);
    }

    private void showNoTestsFoundDialog() {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

            @Override
            public void run() {
                Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                MessageDialog.openWarning(shell,
                        LaunchMessages.Test_Not_Found_Dialog_Title,
                        String.format("%s%n%s", LaunchMessages.Test_Not_Found_Dialog_Message,
                                LaunchMessages.Test_Not_Found_Dialog_Details));

            }
        });
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
