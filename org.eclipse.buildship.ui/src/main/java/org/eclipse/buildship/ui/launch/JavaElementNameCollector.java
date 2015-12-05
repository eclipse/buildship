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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMultimap;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Collects the names of {@link IType} and {@link IMethod} instances.
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
    public static Collection<String> collectClassNamesForTypes(List<IType> types) {
        return FluentIterable.from(types).transform(new Function<IType, String>() {

            @Override
            public String apply(IType type) {
                return type.getFullyQualifiedName();
            }
        }).toSet();
    }

    /**
     * Transforms the target methods to a map where the key is the class name and the value is the collection of method names of that class.
     *
     * @param methods the target methods to transform
     * @return the result method names
     */
    public static Map<String, Collection<String>> collectClassNamesForMethods(List<IMethod> methods) {
        ImmutableMultimap.Builder<String, String> result = ImmutableMultimap.builder();
        for (IMethod method : methods) {
            String typeName = method.getDeclaringType().getFullyQualifiedName();
            String methodName = method.getElementName();
            result.put(typeName, methodName);
        }
        return result.build().asMap();
    }

}
