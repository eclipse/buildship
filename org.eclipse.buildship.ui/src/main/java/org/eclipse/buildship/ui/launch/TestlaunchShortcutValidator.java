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

import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.*;

import java.util.Collection;
import java.util.List;

// todo (etst) DONAT: the class name of Testlaunch should be written in mixed-case in the class name

/**
 * Tests if a set of {@link IJavaElement} instances are valid to launch as tests with.
 */
public final class TestlaunchShortcutValidator {

    private TestlaunchShortcutValidator() {
    }

    /**
     * Validates the target types if they can be used to launch tests.
     *
     * @param elements the target types
     * @return {@code true} if the types can be used to launch tests
     */
    public static boolean validateTypes(Collection<IType> elements) {
        ImmutableList<IType> types = ImmutableList.copyOf(elements);
        return validateJavaElements(types) && validateTypes(types);
    }

    private static boolean validateTypes(ImmutableList<IType> types) {
        for (IType type : types) {
            if (!isInSourceFolder(type)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isInSourceFolder(IType type) {
        // if the type is not defined in a source folder or the source folder
        // type can't be determined, then return false
        IJavaElement fragmentRoot = type.getPackageFragment().getParent();
        if (fragmentRoot instanceof IPackageFragmentRoot) {
            IPackageFragmentRoot packageFragmentRoot = (IPackageFragmentRoot) fragmentRoot;
            try {
                return packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE;
            } catch (JavaModelException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Validates the target methods if they can be used to launch tests.
     *
     * @param elements the target methods
     * @return {@code true} if the types can be used to launch a tests execution
     */
    public static boolean validateMethods(Collection<IMethod> elements) {
        ImmutableList<IMethod> methods = ImmutableList.copyOf(elements);
        return validateJavaElements(methods) && validateMethods(methods);
    }

    private static boolean validateMethods(ImmutableList<IMethod> methods) {
        for (IMethod element : methods) {
            IType type = element.getDeclaringType();
            if (type == null || !isInSourceFolder(type)) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateJavaElements(List<? extends IJavaElement> elements) {
        // at least one java element is present
        if (elements.isEmpty()) {
            return false;
        }

        // all elements have associated projects
        for (IJavaElement element : elements) {
            if (element.getJavaProject() == null || element.getJavaProject().getProject() == null) {
                return false;
            }
        }

        // all elements belong to the same project
        IProject project = elements.get(0).getJavaProject().getProject();
        for (IJavaElement element : elements) {
            if (!element.getJavaProject().getProject().equals(project)) {
                return false;
            }
        }

        // the container project has the Gradle nature
        if (!project.isAccessible() || !GradleProjectNature.INSTANCE.isPresentOn(project)) {
            return false;
        }

        // otherwise the collection is valid
        return true;
    }

    /**
     * Property tester to determine if the test launch shortcut should be visible in the context menus.
     */
    public static final class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

        private static final String PROPERTY_NAME_SELECTION_CAN_BE_LAUNCHED_AS_TEST = "selectioncanbelaunchedastest";

        @Override
        public boolean test(Object receiver, String propertyString, Object[] args, Object expectedValue) {
            if (propertyString.equals(PROPERTY_NAME_SELECTION_CAN_BE_LAUNCHED_AS_TEST)) {
                return receiver instanceof Collection && selectionIsLaunchableAsTest((Collection<?>) receiver);
            } else {
                throw new GradlePluginsRuntimeException("Unrecognized property to test: " + propertyString);
            }
        }

        private boolean selectionIsLaunchableAsTest(Collection<?> elements) {
            JavaElementResolver elementResolver = SelectionJavaElementResolver.from(elements);
            return validateTypes(elementResolver.resolveTypes()) || validateMethods(elementResolver.resolveMethods());
        }

    }

}
