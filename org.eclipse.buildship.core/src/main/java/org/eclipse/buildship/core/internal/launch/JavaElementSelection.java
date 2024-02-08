/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.launch;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;

/**
 * Base class to collect selected {@link IMethod} and {@link IType} instances.
 */
public abstract class JavaElementSelection {

    public final List<String> resolveTests() {
        return ImmutableList.<String>builder()
            .addAll(getSelectedTypes().stream().map(t -> t.getFullyQualifiedName()).collect(Collectors.toList()))
            .addAll(getSelectedMethods().stream().map(m -> m.getDeclaringType().getFullyQualifiedName() + "#" + m.getElementName()).collect(Collectors.toList()))
            .build();
    }

    /**
     * Returns the selected methods.
     * <p>
     * If an item can't be resolved then it is skipped from from the result list.
     *
     * @return the selected {@link IMethod} instances
     */
    public final List<IMethod> getSelectedMethods() {
        ImmutableList.Builder<IMethod> result = ImmutableList.builder();
        for (IJavaElement javaElement : findJavaElements()) {
            Optional<IMethod> method = resolveMethod(javaElement);
            if (method.isPresent()) {
                result.add(method.get());
            }
        }
        return result.build();
    }

    private Optional<IMethod> resolveMethod(IJavaElement javaElement) {
        // exclude methods which have no parent projects
        if (javaElement.getJavaProject() == null || javaElement.getJavaProject().getProject() == null) {
            return Optional.absent();
        }

        if (javaElement instanceof IMethod) {
            IMethod method = (IMethod) javaElement;
            return method.getDeclaringType() != null ? Optional.of(method) : Optional.<IMethod>absent();
        } else {
            return Optional.absent();
        }
    }

    /**
     * Returns the selected types.
     * <p/>
     * For each {@link IMethod} or {@link IField}, the enclosing {@link IType} is returned. If
     * the exact type can't be determined then the top-level type is returned.
     * <p/>
     * If an item can't be resolved then it is skipped from from the result list.
     *
     * @return the resolved {@link IType} instances
     */
    public final List<IType> getSelectedTypes() {
        ImmutableList.Builder<IType> result = ImmutableList.builder();
        for (IJavaElement javaElement : findJavaElements()) {
            Optional<IType> type = resolveType(javaElement);
            if (type.isPresent()) {
                result.add(type.get());
            }
        }
        return result.build();
    }

    private Optional<IType> resolveType(IJavaElement javaElement) {
        // exclude elements with no parent projects
        if (javaElement.getJavaProject() == null || javaElement.getJavaProject().getProject() == null) {
            return Optional.absent();
        }

        IType result = null;
        switch (javaElement.getElementType()) {
            case IJavaElement.TYPE:
                result = (IType) javaElement;
                break;
            case IJavaElement.FIELD:
                result = ((IField) javaElement).getDeclaringType();
                break;
            case IJavaElement.CLASS_FILE:
            case IJavaElement.COMPILATION_UNIT:
                result = ((ITypeRoot) javaElement).findPrimaryType();
                break;
        }

        return Optional.fromNullable(result);
    }

    /**
     * Iterates through the java elements and returns the first non-null container project.
     *
     * @return the container project or absent value if none
     */
    public Optional<IProject> findFirstContainerProject() {
        for (IJavaElement javaElement : findJavaElements()) {
            IJavaProject javaProject = javaElement.getJavaProject();
            if (javaProject != null) {
                IProject project = javaProject.getProject();
                if (project != null) {
                    return Optional.of(project);
                }
            }
        }
        return Optional.absent();
    }

    /**
     * Collects {@link IJavaElement} instances which can be resolved to methods and types.
     *
     * @return the Java elements to be resolved
     */
    protected abstract Collection<IJavaElement> findJavaElements();

}
