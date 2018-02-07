/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.util.classpath;

import java.util.Collections;
import java.util.List;

import org.gradle.tooling.model.eclipse.AccessRule;
import org.gradle.tooling.model.eclipse.ClasspathAttribute;
import org.gradle.tooling.model.eclipse.EclipseClasspathEntry;

import com.google.common.collect.Lists;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.JavaCore;

import org.eclipse.buildship.core.util.gradle.ModelUtils;

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
        List<ClasspathAttribute> attributes = Lists.newArrayList(ModelUtils.getClasspathAttributes(entry).or(Collections.<ClasspathAttribute>emptyList()));
        IClasspathAttribute[] classpathAttributes = new IClasspathAttribute[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            ClasspathAttribute attribute = attributes.get(i);
            classpathAttributes[i] = JavaCore.newClasspathAttribute(attribute.getName(), attribute.getValue());
        }
        return classpathAttributes;
    }
}
