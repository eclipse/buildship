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
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * Collects the names of {@link IType} and {@link IMethod} instances.
 *
 */
public final class JavaElementNameCollector {

    private JavaElementNameCollector() {
    }

    /**
     * Transforms the target types into a collection of fully qualified names.
     *
     * @param types the types to transform
     * @return the result class names
     */
    public static Iterable<String> collectClassNames(List<IType> types) {
        return FluentIterable.from(types).transform(new Function<IType, String>() {

            @Override
            public String apply(IType type) {
                return type.getFullyQualifiedName();
            }
        }).toSet();
    }

    /**
     * Transforms the target methods to a map where the keys are the methods' container class names
     * and the values are the contained method names.
     *
     * @param methods the target methods to transform
     * @return the result method names
     */
    public static Map<String, Iterable<String>> collectClassNamesWithMethods(List<IMethod> methods) {
        Map<String, Collection<String>> testMethods = Maps.newHashMap();
        for (IMethod method : methods) {
            String typeName = method.getDeclaringType().getFullyQualifiedName();
            String methodName = method.getElementName();
            createNewOrAppendToExistingEntry(testMethods, typeName, methodName);
        }
        return ImmutableMap.<String, Iterable<String>>copyOf(testMethods);
    }

    private static void createNewOrAppendToExistingEntry(Map<String, Collection<String>> collection, String key, String valueEntry) {
        Collection<String> value = collection.get(key);
        if (value == null) {
            value = Lists.newArrayList();
            collection.put(key, value);
        }
        value.add(valueEntry);
    }

}
