/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.classpath;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.gradle.tooling.model.eclipse.AccessRule;
import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseClasspathEntry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

/**
 * Contains helper methods for JDT classpath manipulation.
 *
 * @author Donat Csikos
 */
public final class ClasspathUtils {

    private ClasspathUtils() {
    }

    /**
     * Creates JDT access rules for the supplied Gradle classpath entry.
     *
     * @param entry the entry to create the access rules for
     * @return the created array of access rules
     */
    public static IAccessRule[] createAccessRules(EclipseClasspathEntry entry) {
        List<AccessRule> rules = Lists.newArrayList(entry.getAccessRules());
        IAccessRule[] accessRules = new IAccessRule[rules.size()];
        for (int i = 0; i < rules.size(); i++) {
            AccessRule rule = rules.get(i);
            accessRules[i] = JavaCore.newAccessRule(new Path(rule.getPattern()), rule.getKind());
        }
        return accessRules;
    }

    /**
     * Creates JDT classpath attributes for the supplied Gradle classpath entry.
     *
     * @param entry the entry to create the classpath attributes for
     * @return the created array of classpath attributes
     */
    public static IClasspathAttribute[] createClasspathAttributes(EclipseClasspathEntry entry) {
        List<ClasspathAttribute> attributes = Lists.newArrayList(entry.getClasspathAttributes());
        IClasspathAttribute[] classpathAttributes = new IClasspathAttribute[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            ClasspathAttribute attribute = attributes.get(i);
            classpathAttributes[i] = JavaCore.newClasspathAttribute(attribute.getName(), attribute.getValue());
        }
        return classpathAttributes;
    }

    /**
     * Returns the list of Gradle scopes that is defined on the target classpath entry.
     * <p>
     * If the scope information is not available then {@link Optional#absent()} is returned.
     *
     * @param entry the target entry
     * @return the set of scopes
     */
    public static Optional<Set<String>> scopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_scope")) {
                return Optional.<Set<String>>of(Sets.newHashSet(attribute.getValue().split(",")));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the list of Gradle used-by scopes that is defined on the target classpath entry.
     * <p>
     * If the scope information is not available then {@link Optional#absent()} is returned.
     *
     * @param entry the target entry
     * @return the set of used-by scopes
     */
    public static Optional<Set<String>> usedByScopesFor(IClasspathEntry entry) {
        for (IClasspathAttribute attribute : entry.getExtraAttributes()) {
            if (attribute.getName().equals("gradle_used_by_scope")) {
                return Optional.<Set<String>>of(Sets.newHashSet(attribute.getValue().split(",")));
            }
        }
        return Optional.empty();
    }

}
