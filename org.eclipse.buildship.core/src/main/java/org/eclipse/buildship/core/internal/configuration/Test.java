/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.configuration;

import java.util.List;
import java.util.stream.Collectors;

import org.gradle.tooling.TestLauncher;

public abstract class Test {

    public abstract String getSimpleName();

    public abstract String getFullyQualifiedName();

    public abstract void apply(TestLauncher launcher);

    @Override
    public String toString() {
        return getFullyQualifiedName();
    }

    public static List<Test> fromString(List<String> testNames) {
        return testNames.stream().map(t -> {
            int sep = t.indexOf('#') + 1;
            return sep > 0 ? new TestMethod(t.substring(0, sep - 1), t.substring(sep)) : new TestType(t);
        }).collect(Collectors.toList());
    }

    private static class TestType extends Test {

        private String className;

        public TestType(String className) {
            this.className = className;
        }

        @Override
        public String getFullyQualifiedName() {
            return this.className;
        }

        @Override
        public String getSimpleName() {
            return this.className.substring(this.className.lastIndexOf('.') + 1);
        }

        @Override
        public void apply(TestLauncher launcher) {
            launcher.withJvmTestClasses(this.className);
        }
    }

    private static class TestMethod extends Test {

        private final String className;
        private final String methodName;

        public TestMethod(String classfqName, String methodName) {
            this.className = classfqName;
            this.methodName = methodName;
        }

        @Override
        public String getFullyQualifiedName() {
            return this.className + "#" + this.methodName;
        }

        @Override
        public String getSimpleName() {
            return this.methodName;
        }

        @Override
        public void apply(TestLauncher launcher) {
            launcher.withJvmTestMethods(this.className, this.methodName);
        }
    }
}
