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

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Property tester determining whether the current selection in the Java code editor can be used for
 * launching Gradle tests.
 * <p/>
 * Documentation on the core expressions and property testers:
 * http://help.eclipse.org/luna/index.jsp
 * ?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fworkbench_cmd_expressions.htm
 *
 */
public final class GradleLaunchShortcutPropertyTester extends PropertyTester {

    // TODO (donat) at startup the values seem to be not evaluated. maybe we should force
    // re-evaluation on startup
    // http://www.robertwloch.net/2011/01/eclipse-tips-tricks-property-testers-with-command-core-expressions/

    // property name used in the plugin.xml
    public static final String PROPERTY_NAME_CAN_LAUNCH_AS_GRADLE_TEST = "canlaunch";

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        try {
            if (property.equals(PROPERTY_NAME_CAN_LAUNCH_AS_GRADLE_TEST)) {
                return testCanLaunch(receiver, property, args, expectedValue);
            } else {
                throw new IllegalStateException("Unexpected property name: " + property);
            }

        } catch (Exception e) {
            throw new GradlePluginsRuntimeException(e);
        }
    }

    private boolean testCanLaunch(Object receiver, String property, Object[] args, Object expectedValue) throws Exception {
        if (receiver instanceof IEditorInput) {
            return testEditorInputCanLaunch((IEditorInput) receiver);
        } else if (receiver instanceof IJavaElement) {
            return isJavaElementCanBeLaunched((IJavaElement) receiver);
        } else {
            throw new GradlePluginsRuntimeException("Tester got nonexpected receiver: " + receiver.getClass());
        }
    }

    private boolean testEditorInputCanLaunch(IEditorInput editorInput) throws JavaModelException {
        IJavaElement element = JavaUI.getEditorInputJavaElement(editorInput);
        if (element instanceof ICompilationUnit) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window == null) {
                return false;
            }
            ISelection selection = window.getSelectionService().getSelection();
            if (selection != null && selection instanceof ITextSelection) {
                TextSelection textSelection = (TextSelection) selection;
                IJavaElement selectedInText = ((ICompilationUnit) element).getElementAt(textSelection.getOffset());

                // the test is successful when a type or a method is selected
                return isJavaElementCanBeLaunched(selectedInText);
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    private boolean isJavaElementCanBeLaunched(IJavaElement javaElement) throws JavaModelException {
        if (javaElement instanceof IMethod) {
            return isTestMethod((IMethod) javaElement);
        } else {
            return isTestableJavaElement(javaElement);
        }
    }

    private boolean isTestMethod(IMethod method) throws JavaModelException {
        // TODO (donat) are there other constraints when we don't want to enable execution?
        int flags = method.getFlags();
        return Flags.isPublic(flags) && !Flags.isStatic(flags);
    }

    private boolean isTestableJavaElement(IJavaElement javaElement) {
        boolean result = (javaElement instanceof IType) || (javaElement instanceof IPackageFragment) || (javaElement instanceof ICompilationUnit);
        return result;
    }
}