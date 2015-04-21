/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.launch;

import java.util.List;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.configuration.ProjectConfiguration;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.util.file.FileUtils;
import org.eclipse.buildship.core.util.variable.ExpressionUtils;
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.texteditor.ITextEditor;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

/**
 * Launch shortcut to run context-sensitive Gradle tests.
 * <p/>
 * The launch shortcut is available in the context menu under the Run As... entry if and only if a
 * proper selection is active at the time of the invocation.
 */
public final class GradleTestLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            resolveSelectionAndLaunch((IStructuredSelection) selection);
        }
    }

    private void resolveSelectionAndLaunch(IStructuredSelection selection) {
        try {
            Object element = selection.getFirstElement();
            if (element instanceof IJavaElement) {
                resolveJavaElementAndLaunch((IJavaElement) element);
            }
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private void resolveJavaElementAndLaunch(IJavaElement element) throws Exception {
        if (element instanceof ICompilationUnit) {
            ICompilationUnit compilationUnit = (ICompilationUnit) element;
            // take the first top-level type from the source file; no inner types
            IType type = compilationUnit.getTypes()[0];
            launchFilteredTest(type.getJavaProject(), type.getFullyQualifiedName());
        } else if (element instanceof IPackageFragment) {
            IPackageFragment packageFragment = (IPackageFragment) element;
            launchFilteredTest(packageFragment.getJavaProject(), packageFragment.getElementName() + ".*");
        } else if (element instanceof IType) {
            IType type = (IType) element;
            launchFilteredTest(type.getJavaProject(), type.getFullyQualifiedName());
        } else if (element instanceof IMethod) {
            IMethod method = (IMethod) element;
            launchFilteredTest(method.getJavaProject(), method.getDeclaringType().getFullyQualifiedName() + "." + method.getElementName());
        }
 else {
            throw new IllegalStateException("Not supported selection type for launch shortcut: " + element.getClass());
        }
    }

    private void launchFilteredTest(IJavaProject project, String testFilter) {
        // show (but not necessarily activate) the Console view
        WorkbenchUtils.showView(IConsoleConstants.ID_CONSOLE_VIEW, null, IWorkbenchPage.VIEW_VISIBLE);

        // determine the set of attributes that uniquely identify a run configuration
        GradleRunConfigurationAttributes configurationAttributes = getRunConfigurationAttributes(project.getProject(), testFilter);

        // create/reuse a launch configuration for the given attributes
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(configurationAttributes);

        // launch the launch configuration
        DebugUITools.launch(launchConfiguration, ILaunchManager.RUN_MODE);
    }

    protected GradleRunConfigurationAttributes getRunConfigurationAttributes(IProject project, String testFilter) {
        // read the project configuration from the workspace project of the selected nodes (if
        // accessible)
        Optional<FixedRequestAttributes> requestAttributes = getProjectConfigurationRequestAttributes(project.getProject());

        // run only the task 'test' by the shortcut
        ImmutableList<String> tasks = ImmutableList.of("test", "--tests", testFilter);

        // determine the project directory from the selected nodes
        // (we build on the invariant that all selected tasks have the same parent directory and
        // they are all either of type ProjectTaskNode or TaskSelectorNode)
        String projectDirectoryExpression = ExpressionUtils.encodeWorkspaceLocation(project);

        // determine the Gradle distribution
        GradleDistribution gradleDistribution = requestAttributes.isPresent() ? requestAttributes.get().getGradleDistribution() : GradleDistribution.fromBuild();

        // determine the advanced options
        String gradleUserHome = requestAttributes.isPresent() ? FileUtils.getAbsolutePath(requestAttributes.get().getGradleUserHome()).orNull() : null;
        String javaHome = requestAttributes.isPresent() ? FileUtils.getAbsolutePath(requestAttributes.get().getJavaHome()).orNull() : null;
        List<String> jvmArguments = requestAttributes.isPresent() ? requestAttributes.get().getJvmArguments() : ImmutableList.<String> of();

        // add extra default arguments to run only a subset of the tests
        List<String> arguments = requestAttributes.isPresent() ? requestAttributes.get().getArguments() : ImmutableList.<String> of();
        // create the run configuration with test progress visualization enabled by default
        return GradleRunConfigurationAttributes.with(tasks, projectDirectoryExpression, gradleDistribution, gradleUserHome, javaHome, jvmArguments, arguments, true);
    }

    private Optional<FixedRequestAttributes> getProjectConfigurationRequestAttributes(IProject project) {
        if (project.isOpen() && GradleProjectNature.INSTANCE.isPresentOn(project)) {
            ProjectConfiguration projectConfiguration = CorePlugin.projectConfigurationManager().readProjectConfiguration(project);
            return Optional.of(projectConfiguration.getRequestAttributes());
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        try {
            if (editor instanceof ITextEditor) {
                ITextEditor textEditor = (ITextEditor) editor;
                IJavaElement element = JavaUI.getEditorInputJavaElement(textEditor.getEditorInput());
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window != null) {
                    ISelection selection = window.getSelectionService().getSelection();
                    if (selection != null && selection instanceof ITextSelection) {
                        TextSelection textSelection = (TextSelection) selection;
                        IJavaElement selectedInText = ((ICompilationUnit) element).getElementAt(textSelection.getOffset());
                        resolveJavaElementAndLaunch(selectedInText);
                    }
                }
            }
        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

}