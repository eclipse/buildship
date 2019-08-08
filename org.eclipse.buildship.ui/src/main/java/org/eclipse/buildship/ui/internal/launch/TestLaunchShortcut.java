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

package org.eclipse.buildship.ui.internal.launch;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.GradleDistribution;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.launch.GradleTestRunConfigurationAttributes;
import org.eclipse.buildship.core.internal.launch.RunGradleJvmTestLaunchRequestJob;
import org.eclipse.buildship.core.internal.util.variable.ExpressionUtils;

/**
 * Shortcut for Gradle test launches from the Java editor or from the current selection.
 */
public final class TestLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        JavaElementResolver resolver = SelectionJavaElementResolver.from(selection);
        launch(resolver, mode);
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        JavaElementResolver resolver = EditorJavaElementResolver.from(editor);
        launch(resolver, mode);
    }

    private void launch(JavaElementResolver resolver, String mode) {
        List<IType> types = resolver.resolveTypes();
        List<IMethod> methods = resolver.resolveMethods();
        if (TestLaunchShortcutValidator.validateTypesAndMethods(types, methods)) {
            IProject project = findProject(types, methods);
            GradleTestRunConfigurationAttributes attributes = createLaunchConfigAttributes(project, resolver.resolveTests());
            ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateTestRunConfiguration(attributes);
            new RunGradleJvmTestLaunchRequestJob(launchConfiguration, mode).schedule();
        } else {
            showNoTestsFoundDialog();
        }
    }

    private static GradleTestRunConfigurationAttributes createLaunchConfigAttributes(IProject project, List<String> tests) {
        return new GradleTestRunConfigurationAttributes(ExpressionUtils.encodeWorkspaceLocation(project),
                                                    GradleDistribution.fromBuild().toString(),
                                                    null,
                                                    null,
                                                    Collections.emptyList(),
                                                    Collections.emptyList(),
                                                    false,
                                                    false,
                                                    false,
                                                    false,
                                                    false,
                                                    tests);
    }

    private IProject findProject(List<IType> types, List<IMethod> methods) {
        for (IType t : types) {
            return t.getJavaProject().getProject();
        }

        for (IMethod m : methods) {
            return m.getJavaProject().getProject();
        }

        throw new GradlePluginsRuntimeException("Empty selection should not be valid for test launch shortcut");
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
}
