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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Base class to resolve {@link IMethod} and {@link IType} instances.
 */
public abstract class JavaElementResolver {

    /**
     * Collects {@link IJavaElement} instances which can be resolved to methods and types.
     *
     * @return the Java elements to be resolved
     */
    protected abstract Collection<IJavaElement> findJavaElements();

    /**
     * Resolves the items returned by {@link #findJavaElements()} to {@link IMethod} instances.
     * <p/>
     * If an item can't be resolved then it is skipped from from the result list.
     *
     * @return the resolved {@link IMethod} instances
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
            if (method.getDeclaringType() == null || !isInSourceFolder(method.getDeclaringType(), method.getJavaProject())) {
                return Optional.absent();
            } else {
                return Optional.of(method);
            }
        } else {
            return Optional.absent();
        }
    }
    
    private boolean isInSourceFolder(IType type, IJavaProject project) {
        // if the type is not defined in a source folder or the source folder 
        // type can't be determined, then return false
        IJavaElement fragmentRoot = type.getPackageFragment().getParent();
        if (fragmentRoot != null && fragmentRoot instanceof IPackageFragmentRoot) {
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
     * Resolves the items returned by {@link #findJavaElements()} to {@link IType} instances.
     * <p/>
     * For each {@link IMethod} or {@link IField}, then the enclosing {@link IType} is returned. If
     * the exact type can't be determined then the top-level type is returned.
     * <p/>
     * If an item can't be resolved then it is skipped from from the result list.
     *
     * @return the resolved {@link IType} instances
     */
    public final List<IType> resolveTypes() {
        ImmutableList.Builder<IType> result = ImmutableList.builder();
        for (IJavaElement javaElement : findJavaElements()) {
            Optional<IType> type = resolveType(javaElement);
            if (type.isPresent()){
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
            case IJavaElement.METHOD:
                result = ((IMethod) javaElement).getDeclaringType();
                break;
        }
        
        // if the type could be found but it is not defined in a source folder (class in a 
        // dependency jar) then don't resolve it
        if (result != null && !isInSourceFolder(result, javaElement.getJavaProject())) {
            result = null;
        }
        
        return Optional.fromNullable(result);
    }

}
