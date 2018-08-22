/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.launch.impl;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

/**
 * Lists all the launch configuration types that {@link GradleClasspathProvider} can be used with.
 *
 * @author Donat Csikos
 */
public enum SupportedLaunchConfigType {

    JDT_JAVA_APPLICATION("org.eclipse.jdt.launching.localJavaApplication") {

        @Override
        protected Set<IPackageFragmentRoot> getSourceFolders(ILaunchConfiguration configuration) throws CoreException {
            JavaLaunchDelegate launchDelegate = new JavaLaunchDelegate();
            IJavaProject javaProject = launchDelegate.getJavaProject(configuration);
            if (javaProject == null) {
                return ImmutableSet.of();
            }

            String mainTypeName = launchDelegate.getMainTypeName(configuration);
            if (mainTypeName == null) {
                return ImmutableSet.of();
            }

            IType mainType = javaProject.findType(mainTypeName);
            if (mainType == null) {
                return ImmutableSet.of();
            }

            IJavaElement pkg = mainType.getPackageFragment().getParent();
            if (!(pkg instanceof IPackageFragmentRoot)) {
                return ImmutableSet.of();
            }

            return ImmutableSet.of((IPackageFragmentRoot) pkg);
        }

    },

    JDT_JUNIT("org.eclipse.jdt.junit.launchconfig") {

        @Override
        protected Set<IPackageFragmentRoot> getSourceFolders(ILaunchConfiguration configuration) throws CoreException {
            RelaxedJUnitLaunchConfigurationDelegate launchDelegate = new RelaxedJUnitLaunchConfigurationDelegate();
            IMember[] members = launchDelegate.evaluateTests(configuration);

            Builder<IPackageFragmentRoot> result = ImmutableSet.builder();
            for (IMember member : members) {
                IType type = member.getDeclaringType();
                if (type == null) {
                    if (member instanceof IType) {
                        type = (IType) member;
                    } else {
                        continue;
                    }
                }

                if (type != null) {
                    IJavaElement pkg = type.getPackageFragment().getParent();
                    if (!(pkg instanceof IPackageFragmentRoot)) {
                        continue;
                    }
                    result.add((IPackageFragmentRoot) pkg);
                }
            }
            return result.build();
        }
    };

    private final String id;

    SupportedLaunchConfigType(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static Set<IPackageFragmentRoot> collectSourceFolders(ILaunchConfiguration configuration) throws CoreException {
        SupportedLaunchConfigType type = launchConfigTypeFor(configuration.getType().getIdentifier());
        if (type == null) {
            return ImmutableSet.of();
        }
        return type.getSourceFolders(configuration);
    }

    public static boolean isSupported(ILaunchConfiguration configuration) {
        try {
            return launchConfigTypeFor(configuration.getType().getIdentifier()) != null;
        } catch (CoreException e) {
            return false;
        }
    }

    private static SupportedLaunchConfigType launchConfigTypeFor(String typeId) {
        for (SupportedLaunchConfigType type : values()) {
            if (type.getId().equals(typeId)) {
                return type;
            }
        }
        return null;
    }

    protected abstract Set<IPackageFragmentRoot> getSourceFolders(ILaunchConfiguration configuration) throws CoreException;

    /**
     * Helper class to access the members referenced by a JUnit launch configuration as the
     * corresponding {@code evaluateTests()} method is protected by default.
     */
    private static class RelaxedJUnitLaunchConfigurationDelegate extends JUnitLaunchConfigurationDelegate {

        public IMember[] evaluateTests(ILaunchConfiguration configuration) throws CoreException {
            return evaluateTests(configuration, new NullProgressMonitor());
        }
    }
}
