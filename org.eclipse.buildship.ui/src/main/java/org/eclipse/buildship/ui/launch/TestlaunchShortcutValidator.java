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

import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.core.configuration.GradleProjectNature;

/**
 * Tests if a set of {@link IJavaElement} is valid to launch tests with.
 */
public final class TestlaunchShortcutValidator {

    private TestlaunchShortcutValidator() {
    }

    /**
     * Validates the target Java elements if they can be used to launch tests.
     *
     * @param javaElements the target elements
     * @return {@code true} if the elements can be used to launch a tests execution
     */
    public static boolean validateElements(Collection<? extends IJavaElement> javaElements) {
        ImmutableList<IJavaElement> elements = ImmutableList.copyOf(javaElements);

        // tests can be launched if there are classes or methods,
        if (elements.isEmpty()) {
            return false;
        }

        // all elements have a container project,
        for (IJavaElement element : elements) {
            if (element.getJavaProject() == null || element.getJavaProject().getProject() == null) {
                return false;
            }
        }

        // all elements have the same container project, and
        IProject project = elements.get(0).getJavaProject().getProject();
        for (int i = 0; i < javaElements.size(); i++) {
            if (!elements.get(i).getJavaProject().getProject().equals(project)) {
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
     * Property tester to determine if the test launch shortcut should be visible in the context
     * menus.
     */
    @SuppressWarnings("unused")
    public static final class LaunchShortcutPropertyTester extends PropertyTester {

        private static final String PROPERTY_SELECTION_CAN_BE_LAUNCHED_AS_TEST = "selectioncanbelaunchedastest";

        @Override
        public boolean test(Object receiver, String propertyString, Object[] args, Object expectedValue) {
            if (propertyString.equals(PROPERTY_SELECTION_CAN_BE_LAUNCHED_AS_TEST)) {
                return receiver instanceof Collection<?> && selectionCanBeLaunchedAsTest((Collection<?>) receiver);
            } else {
                throw new GradlePluginsRuntimeException("Not recognized property to test: " + propertyString);
            }
        }

        private static boolean selectionCanBeLaunchedAsTest(Collection<? extends Object> elements) {
            JavaElementResolver elementResolver = SelectionJavaElementResolver.from(elements);
            List<IType> resolveTypes = elementResolver.resolveTypes();
            return validateElements(elementResolver.resolveTypes()) || validateElements(elementResolver.resolveMethods());
        }

    }

}
