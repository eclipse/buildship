/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.launch.internal;

import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.junit.launcher.JUnitLaunchConfigurationDelegate;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Contains utility methods to collect Gradle source set information.
 *
 * @author Donat Csikos
 */
public final class SourceSetCollector {

    // TODO (donat) clean up

    private SourceSetCollector() {
    }

    /**
     * Finds the main class referenced by the target launch configuration and collects the Gradle
     * source sets that contains the class.
     * <p/>
     * If the main class cannot be obtained (e.g. the target configuration is not a JDT launch) or
     * if source folder of the class doesn't define any information about the Gradle source sets
     * then the method returns and empty set. Upon exception the message is logged and an empty set
     * is returned.
     *
     * @param configuration the target launch configuration
     * @return the source set names
     */
    public static Set<String> mainClassSourceSets(ILaunchConfiguration configuration) {
        try {
            System.err.println(configuration.getType().getIdentifier());
            if (DefaultExternalLaunchConfigurationManager.LAUNCH_CONFIG_TYPE_JUNIT_LAUNCH.equals(configuration.getType().getIdentifier())) {
                RelaxedJUnitLaunchConfigurationDelegate launchDelegate = new RelaxedJUnitLaunchConfigurationDelegate();
                IMember[] members = launchDelegate.evaluateTests(configuration);

                Set<String> gradleSourceSets = Sets.newHashSet();
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
                        gradleSourceSets.addAll(usedByScopesFor(((IPackageFragmentRoot) pkg).getRawClasspathEntry()));
                    }
                }

                System.err.println("source sets for junit execution =" + gradleSourceSets);
                return gradleSourceSets;
            }

            if (DefaultExternalLaunchConfigurationManager.LAUNCH_CONFIG_TYPE_JAVA_LAUNCH.equals(configuration.getType().getIdentifier())) {
                JavaLaunchDelegate launchDelegate = new JavaLaunchDelegate();
                IJavaProject javaProject = launchDelegate.getJavaProject(configuration);
                if (javaProject == null) {
                    return Collections.emptySet();
                }

                String mainTypeName = launchDelegate.getMainTypeName(configuration);
                if (mainTypeName == null) {
                    return Collections.emptySet();
                }

                IType mainType = javaProject.findType(mainTypeName);
                if (mainType == null) {
                    return Collections.emptySet();
                }

                IJavaElement pkg = mainType.getPackageFragment().getParent();
                if (!(pkg instanceof IPackageFragmentRoot)) {
                    return Collections.emptySet();
                }

                return usedByScopesFor(((IPackageFragmentRoot) pkg).getRawClasspathEntry());
            }

            else {
                return Collections.emptySet();
            }
        } catch (CoreException e) {
            CorePlugin.logger().warn("Cannot collect source set information for dependencies", e);
        }
        return Collections.emptySet();
    }

    /**
     * Determines whether any of the specified source sets contain the target classpath entry.
     * <p/>
     * If the entry doesn't define source set information or the source set names are empty then
     * this method returns {@code true}.
     *
     * @param entry the target classpath entry
     * @param sourceSetNames the name of the source sets to look for
     * @return
     */
    public static boolean isEntryInSourceSets(IClasspathEntry entry, Set<String> sourceSetNames) {
        if (sourceSetNames.isEmpty()) {
            return true;
        }

        Set<String> librarySourceSets = scopesFor(entry);
        if (librarySourceSets.isEmpty()) {
            return true;
        }

        return !Sets.intersection(sourceSetNames, librarySourceSets).isEmpty();
    }

    private static Set<String> scopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_scope")) {
                return Sets.newHashSet(attribute.getValue().split(","));
            }
        }
        return Collections.emptySet();
    }

    private static Set<String> usedByScopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_used_by_scope")) {
                return Sets.newHashSet(attribute.getValue().split(","));
            }
        }
        return Collections.emptySet();
    }

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
