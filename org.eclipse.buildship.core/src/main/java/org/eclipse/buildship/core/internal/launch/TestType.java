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

package org.eclipse.buildship.core.internal.launch;

import org.gradle.tooling.TestLauncher;

import com.google.common.base.Preconditions;

import org.eclipse.jdt.core.IType;

/**
 * {@link TestTarget} implementation backed by an {@link IType} instance.
 */
public final class TestType implements TestTarget {

    private final IType type;

    private TestType(IType type) {
        this.type = Preconditions.checkNotNull(type);
    }

    @Override
    public String getSimpleName() {
        return this.type.getElementName();
    }

    @Override
    public String getQualifiedName() {
        return this.type.getFullyQualifiedName();
    }

    @Override
    public void apply(TestLauncher launcher) {
        launcher.withJvmTestClasses(this.type.getFullyQualifiedName());
    }

    public static TestType from(IType type) {
        return new TestType(type);
    }

}
