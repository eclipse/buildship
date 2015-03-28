/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild.testing;

import java.io.File;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.gradle.api.file.EmptyFileVisitor;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.internal.tasks.testing.DefaultTestClassRunInfo;
import org.gradle.api.internal.tasks.testing.TestClassProcessor;
import org.gradle.api.internal.tasks.testing.TestClassRunInfo;

public final class EclipsePluginTestClassScanner implements Runnable {

    private final FileTree candidateClassFiles;
    private final TestClassProcessor testClassProcessor;

    public EclipsePluginTestClassScanner(FileTree candidateClassFiles, TestClassProcessor testClassProcessor) {
        this.candidateClassFiles = candidateClassFiles;
        this.testClassProcessor = testClassProcessor;
    }

    @Override
    public void run() {
        this.candidateClassFiles.visit(new ClassFileVisitor() {

            @Override
            public void visitClassFile(FileVisitDetails fileDetails) {
                String className = fileDetails.getRelativePath().getPathString().replaceAll("\\.class", "").replace('/', '.');
                TestClassRunInfo testClass = new DefaultTestClassRunInfo(className);
                EclipsePluginTestClassScanner.this.testClassProcessor.processTestClass(testClass);
            }
        });
    }

    private abstract class ClassFileVisitor extends EmptyFileVisitor {

        @Override
        public void visitFile(FileVisitDetails fileDetails) {
            final File file = fileDetails.getFile();
            if (isValidTestClassFile(file)) {
                visitClassFile(fileDetails);
            }
        }

        private boolean isValidTestClassFile(final File file) {
            try {
                return isTopLevelClass(file) && isConcreteClass(file);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean isTopLevelClass(final File file) {
            return file.getAbsolutePath().endsWith(".class") && !file.getAbsolutePath().contains("$");
        }

        private boolean isConcreteClass(File file) throws Exception {
            ClassParser parser = new ClassParser(file.getAbsolutePath());
            JavaClass javaClass = parser.parse();
            return !javaClass.isAbstract() && !javaClass.isInterface();
        }

        public abstract void visitClassFile(FileVisitDetails fileDetails);
    }
}
