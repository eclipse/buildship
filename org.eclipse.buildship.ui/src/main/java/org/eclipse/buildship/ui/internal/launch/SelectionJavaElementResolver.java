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

package org.eclipse.buildship.ui.internal.launch;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

import java.util.Collection;
import java.util.Collections;

/**
 * Resolves elements from the current selection.
 */
public final class SelectionJavaElementResolver extends JavaElementResolver {

    private final Collection<?> adaptables;

    private SelectionJavaElementResolver(Collection<?> adaptables) {
        this.adaptables = ImmutableList.copyOf(adaptables);
    }

    @Override
    protected Collection<IJavaElement> findJavaElements() {
        return FluentIterable.from(this.adaptables).transform(new Function<Object, IJavaElement>() {

            @Override
            @SuppressWarnings({"cast", "RedundantCast"})
            public IJavaElement apply(Object input) {
                return (IJavaElement) Platform.getAdapterManager().getAdapter(input, IJavaElement.class);
            }

        }).filter(Predicates.notNull()).toList();
    }

    /**
     * Creates a new instance.
     *
     * @param selection selection to resolve the Java elements from
     * @return the new instance
     */
    public static SelectionJavaElementResolver from(ISelection selection) {
        Collection<?> adaptables = selection instanceof IStructuredSelection ? ((StructuredSelection) selection).toList() : Collections.emptyList();
        return new SelectionJavaElementResolver(adaptables);
    }

    /**
     * Creates a new instance.
     *
     * @param adaptables the collection to resolve the Java elements from
     * @return the new instance
     */
    public static SelectionJavaElementResolver from(Collection<?> adaptables) {
        return new SelectionJavaElementResolver(adaptables);
    }

}
