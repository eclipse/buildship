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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;

/**
 * Base class to resolve {@link IJavaElement}s from various sources to {@link IMethod} and
 * {@link IType} instances.
 */
public abstract class JavaElementResolver {

    /**
     * Collect {@link IJavaElement} instances from the data source.
     *
     * @return the Java elements to convert
     */
    protected abstract Collection<IJavaElement> findJavaElements();

    /**
     * Resolves the Java elements from returned by {@link #findJavaElements()} to {@link IMethod}
     * instances.
     * <p/>
     * If a resolution can't be performed the item is skipped from the result.
     *
     * @return the list of resolved {@link IMethod} instances
     */
    public final List<IMethod> resolveMethods() {
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

            // exclude methods without a declaring type
            if (method.getDeclaringType() == null) {
                return Optional.absent();
            } else {
                return Optional.of(method);
            }
        } else {
            return Optional.absent();
        }
    }

    /**
     * Resolves the Java elements from returned by {@link #findJavaElements()} to {@link IType}
     * instances.
     * <p/>
     * For each {@link IMethod} or {@link IField}, then the enclosing {@link IType} is returned. If
     * the exact type can't be determined for an item, then the top-level type is returned.
     * <p/>
     * If a resolution can't be performed the item is skipped from the result.
     *
     * @return the list of resolved {@link IType} instances
     */
    public final List<IType> resolveTypes() {
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
        // exclude elements which have no parent projects
        if (javaElement.getJavaProject() == null || javaElement.getJavaProject().getProject() == null) {
            return Optional.absent();
        }

        switch (javaElement.getElementType()) {
            case IJavaElement.TYPE:
                return Optional.of((IType) javaElement);
            case IJavaElement.FIELD:
                return Optional.fromNullable(((IField) javaElement).getDeclaringType());
            case IJavaElement.CLASS_FILE:
            case IJavaElement.COMPILATION_UNIT:
                return Optional.fromNullable(((ITypeRoot) javaElement).findPrimaryType());
            case IJavaElement.METHOD:
                return Optional.fromNullable(((IMethod) javaElement).getDeclaringType());
            default:
                return Optional.absent();
        }
    }

}
