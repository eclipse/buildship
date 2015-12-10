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

package org.eclipse.buildship.core.launch;

import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingclient.TestConfig.Builder;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;

/**
 * {@link TestTarget} implementation backed by an {@link IMethod} instance.
 */
public final class TestMethod implements TestTarget {

    private final IMethod method;

    private TestMethod(IMethod method) {
        this.method = Preconditions.checkNotNull(method);
    }

    @Override
    public String getSimpleName() {
        return method.getElementName();
    }

    @Override
    public String getQualifiedName() {
        IType declaringType = method.getDeclaringType();
        return declaringType.getElementName() + "#" + method.getElementName();
    }

    @Override
    public void apply(Builder testConfig) {
        IType declaringType = method.getDeclaringType();
        String typeName = declaringType.getFullyQualifiedName();
        String methodName = method.getElementName();
        testConfig.jvmTestMethods(typeName, methodName);
    }

    public static TestMethod from(IMethod method) {
        return new TestMethod(method);
    }
}