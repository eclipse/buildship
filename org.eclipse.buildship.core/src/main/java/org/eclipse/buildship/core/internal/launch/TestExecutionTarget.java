/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.internal.configuration.GradleProjectNature;
import org.eclipse.buildship.core.internal.configuration.Test;
import org.eclipse.buildship.core.internal.preferences.PersistentModel;
import org.eclipse.buildship.core.internal.util.classpath.ClasspathUtils;

public abstract class TestExecutionTarget {

    private TestExecutionTarget() {
    }

    public abstract IProject getProject();

    public abstract Optional<String> validate();

    private static class SelectionBackedTestExecutionTarget extends TestExecutionTarget {

        private final JavaElementSelection selection;
        private final String mode;
        private final IProject project; // can be null/incorrect when validation fails

        public SelectionBackedTestExecutionTarget(JavaElementSelection selection, String mode) {
            this.selection = selection;
            this.mode = mode;
            this.project = ImmutableList.<IJavaElement>builder()
                    .addAll(selection.getSelectedTypes())
                    .addAll(selection.getSelectedMethods())
                    .build()
                    .stream()
                    .findFirst()
                    .map(javaElement -> javaElement.getJavaProject())
                    .map(javaProject -> javaProject.getProject())
                    .orElse(null);
        }

        @Override
        public Optional<String> validate() {
            Optional<String> result = canExecuteTestRun(this.selection.getSelectedTypes(), this.selection.getSelectedMethods());
            if (!result.isPresent() && "debug".equals(this.mode)) { //$NON-NLS-1$
                result = supportsTestDebugging(this.project);
            }
            return result;
        }

        @Override
        public IProject getProject() {
            return this.project;
        }
    }

    private static class TestBackedTestExecutionTarget extends TestExecutionTarget {

        private final IProject project;
        private final List<Test> tests;
        private final String mode;

        public TestBackedTestExecutionTarget(IProject project, List<Test> tests, String mode) {
            this.project = project;
            this.tests = tests;
            this.mode = mode;
        }

        @Override
        public Optional<String> validate() {
            Optional<String> result = validateGradleProject(this.project);
            if (!result.isPresent()) {
                result = this.tests.isEmpty() ? Optional.of(NLS.bind(LaunchMessages.Validation_Message_NoTests_0, this.project.getName())) : Optional.empty();
            }
            if (!result.isPresent()) {
                result = "debug".equals(this.mode) ? supportsTestDebugging(this.project) : Optional.empty(); //$NON-NLS-1$
            }

            return result;
        }

        @Override
        public IProject getProject() {
            return this.project;
        }
    }

    private static Optional<String> canExecuteTestRun(Collection<IType> types, Collection<IMethod> methods) {
        List<IJavaElement> allElements = ImmutableList.<IJavaElement>builder().addAll(types).addAll(methods).build();
        Optional<String> result = validateJavaElements(allElements);
        if (!result.isPresent()) {
            result = validateTypes(types);
        }
        if (!result.isPresent()) {
            result =  validateMethods(methods);
        }
        return result;
    }

    private static Optional<String> validateTypes(Collection<IType> types) {
        for (IType type : types) {
            if (!isInSourceFolder(type)) {
                return Optional.of(LaunchMessages.Validation_Message_BinaryType);
            }
        }

        for (IType type : types) {
            if (!isTestType(type)) {
                return Optional.of(LaunchMessages.Validation_Message_NotTestType);
            }
        }

        return Optional.empty();
    }

    private static Optional<String> validateMethods(Collection<IMethod> methods) {
        for (IMethod element : methods) {
            IType type = element.getDeclaringType();
            if (type == null || !isInSourceFolder(type)) {
                return Optional.of(LaunchMessages.Validation_Message_BinaryMethod);
            }
        }
        return Optional.empty();
    }

    private static Optional<String> supportsTestDebugging(IProject project) {
        PersistentModel model = CorePlugin.modelPersistence().loadModel(project);
        return  model.isPresent() && model.getGradleVersion().supportsTestDebugging()
                ? Optional.empty()
                : Optional.of(NLS.bind(LaunchMessages.Validation_Message_NoTestDebugSupport_0_1, model.getGradleVersion().getVersion(), project.getName()));
    }

    private static boolean isInSourceFolder(IType type) {
        // if the type is not defined in a source folder or the source folder
        // type can't be determined, then return false
        IPackageFragmentRoot packageFragmentRoot = getPackageFragmentRoot(type);
        if (packageFragmentRoot != null) {
            try {
                return packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE && packageFragmentRoot.getRawClasspathEntry() != null;
            } catch (JavaModelException e) {
                return false;
            }
        }

        return false;
    }

    private static IPackageFragmentRoot getPackageFragmentRoot(IType type) {
        IJavaElement fragmentRoot = type.getPackageFragment().getParent();
        if (fragmentRoot instanceof IPackageFragmentRoot) {
            return (IPackageFragmentRoot) fragmentRoot;
        }

        return null;
    }

    private static Optional<String> validateJavaElements(List<? extends IJavaElement> elements) {
        // at least one java element is present
        if (elements.isEmpty()) {
            return Optional.of(LaunchMessages.Validation_Message_NoTests);
        }

        // all elements have associated projects
        for (IJavaElement element : elements) {
            if (element.getJavaProject() == null || element.getJavaProject().getProject() == null) {
                return Optional.of(LaunchMessages.Validation_Message_NoProject);
            }
        }

        // all elements belong to the same project
        IProject project = elements.get(0).getJavaProject().getProject();
        for (IJavaElement element : elements) {
            if (!element.getJavaProject().getProject().equals(project)) {
                return Optional.of(LaunchMessages.Validation_Message_DifferentProject);
            }
        }

        // the container project has the Gradle nature
        return validateGradleProject(project);
    }

    private static Optional<String> validateGradleProject(IProject project) {
        if (project == null) {
            return Optional.of(LaunchMessages.Validation_Message_NullProject);
        } else if (!project.isOpen()) {
            return Optional.of(NLS.bind(LaunchMessages.Validation_Message_ClosedProject_0, project.getName())); //$NON-NLS-2$
        }

        return GradleProjectNature.isPresentOn(project) ? Optional.empty() : Optional.of(NLS.bind(LaunchMessages.Validation_Message_NotGradleProject_0, project.getName()));
    }

    private static boolean isTestType(IType type) {
        IClasspathEntry fragmentRootEntry = null;
        try {
            fragmentRootEntry = getPackageFragmentRoot(type).getRawClasspathEntry();
        } catch (JavaModelException e) {
            throw new GradlePluginsRuntimeException(e);
        }

        Optional<Set<String>> scopes = ClasspathUtils.scopesFor(fragmentRootEntry);
        if (scopes.isPresent()) {
            return hasElementContainingTheWordTest(scopes.get());
        } else {
            return hasElementContainingTheWordTest(projectRelativePathSegments(type));
        }
    }

    private static List<String> projectRelativePathSegments(IType type) {
        return Arrays.asList(type.getPath().makeRelativeTo(type.getJavaProject().getProject().getFullPath()).segments());
    }

    private static boolean hasElementContainingTheWordTest(Collection<String> elements) {
        for (String element : elements) {
            if (element.toLowerCase().contains("test")) { //$NON-NLS-1$
                return true;
            }
        }
        return false;
    }

    public static TestExecutionTarget from(JavaElementSelection selection, String mode) {
        return new SelectionBackedTestExecutionTarget(selection, mode);
    }

    public static TestExecutionTarget from(IProject project, List<Test> tests, String mode) {
        return new TestBackedTestExecutionTarget(project, tests, mode);
    }
}
