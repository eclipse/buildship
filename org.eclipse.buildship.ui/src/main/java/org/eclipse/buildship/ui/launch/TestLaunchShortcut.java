/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.launch;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.launch.RunGradleJvmTestLaunchRequestJob;
import org.eclipse.buildship.core.launch.RunGradleJvmTestMethodLaunchRequestJob;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

/**
 * This {@link ILaunchShortcut} is used to offer the opportunity to run tests
 * from the editor or package explorer.
 */
@SuppressWarnings("restriction")
public class TestLaunchShortcut implements ILaunchShortcut {

    @Override
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            launch(((IStructuredSelection) selection).toList(), mode);
        } else {
            logNoTestsFound();
        }
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        ITypeRoot element = JavaUI.getEditorInputTypeRoot(editor.getEditorInput());
        if (element != null) {
            Optional<IMethod> selectedMethod = resolveSelectedMethodName(editor, element);
            if (selectedMethod.isPresent()) {
                launch(ImmutableList.of(selectedMethod.get()), mode);
            } else {
                launch(ImmutableList.of(element), mode);
            }
        } else {
            logNoTestsFound();
        }
    }

    protected void performLaunch(List<? extends IJavaElement> elementsToLaunch, String mode) {
        // get the right attributes for the GradleRunConfigurationAttributes
        IJavaElement javaElement = elementsToLaunch.get(0);
        IProject project = javaElement.getResource().getProject();

        FixedRequestAttributes attributes = CorePlugin.projectConfigurationManager().readProjectConfiguration(project)
                .getRequestAttributes();

        String gradleUserHome = attributes.getGradleUserHome() != null
                ? attributes.getGradleUserHome().getAbsolutePath() : null;
        String javaHome = attributes.getJavaHome() != null ? attributes.getJavaHome().getAbsolutePath() : null;

        GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.with(
                ImmutableList.<String> of(), attributes.getProjectDir().getAbsolutePath(),
                attributes.getGradleDistribution(), gradleUserHome, javaHome, attributes.getJvmArguments(),
                attributes.getArguments(), true, true);

        Job runTestsJob = null;
        if (containsMethods(elementsToLaunch)) {
            runTestsJob = new RunGradleJvmTestMethodLaunchRequestJob(getClassNamesWithMethods(elementsToLaunch),
                    configurationAttributes);
        } else {
            runTestsJob = new RunGradleJvmTestLaunchRequestJob(getClassNames(elementsToLaunch),
                    configurationAttributes);
        }
        runTestsJob.schedule();
    }

    private Iterable<String> getClassNames(List<? extends IJavaElement> elementsToLaunch) {
        return FluentIterable.from(elementsToLaunch).filter(IType.class)
                .transform(new Function<IJavaElement, String>() {

                    @Override
                    public String apply(IJavaElement javaElement) {
                        return ((IType) javaElement).getFullyQualifiedName();
                    }
                }).toSet();
    }

    private Map<String, Iterable<String>> getClassNamesWithMethods(List<? extends IJavaElement> elementsToLaunch) {

        Map<String, Collection<String>> testMethods = Maps.newHashMap();

        for (IJavaElement javaElement : elementsToLaunch) {
            if (IJavaElement.METHOD == javaElement.getElementType()) {
                IMethod method = (IMethod) javaElement;
                fillClassNameWithMethodMap(testMethods, method);
            } else if (IJavaElement.TYPE == javaElement.getElementType()) {
                IType type = (IType) javaElement;
                try {
                    IMethod[] methods = type.getMethods();
                    for (IMethod method : methods) {
                        fillClassNameWithMethodMap(testMethods, method);
                    }
                } catch (JavaModelException e) {
                    // Only log as info and simply continue
                    UiPlugin.logger().info(e.getMessage(), e);
                }
            }
        }

        return ImmutableMap.<String, Iterable<String>> copyOf(testMethods);
    }

    private void fillClassNameWithMethodMap(Map<String, Collection<String>> testMethods, IMethod method) {
        Collection<String> methodNames = testMethods.get(method.getDeclaringType().getFullyQualifiedName());
        if (methodNames == null) {
            methodNames = Lists.newArrayList();
            testMethods.put(method.getDeclaringType().getFullyQualifiedName(), methodNames);
        }
        methodNames.add(method.getElementName());
    }

    private boolean containsMethods(List<? extends IJavaElement> javaElements) {
        return FluentIterable.from(javaElements).anyMatch(new Predicate<IJavaElement>() {

            @Override
            public boolean apply(IJavaElement javaElement) {
                return IJavaElement.METHOD == javaElement.getElementType();
            }
        });
    }

    private void launch(List<?> elements, String mode) {
        Builder<IJavaElement> builder = ImmutableList.builder();

        for (Object selected : elements) {
            IJavaElement element = (IJavaElement) Platform.getAdapterManager().getAdapter(selected, IJavaElement.class);

            if (element != null) {
                Optional<? extends IJavaElement> elementToLaunch = getElementToLaunch(element);
                if (elementToLaunch.isPresent()) {
                    builder.add(elementToLaunch.get());
                }
            }
        }

        ImmutableList<IJavaElement> elementsToLaunch = builder.build();

        if (elementsToLaunch.isEmpty()) {
            logNoTestsFound();
            return;
        }
        performLaunch(elementsToLaunch, mode);
    }

    private Optional<? extends IJavaElement> getElementToLaunch(IJavaElement element) {
        switch (element.getElementType()) {
        case IJavaElement.CLASS_FILE:
            return Optional.fromNullable(((IClassFile) element).getType());
        case IJavaElement.COMPILATION_UNIT:
            return Optional.fromNullable(((ICompilationUnit) element).findPrimaryType());
        }
        return Optional.of(element);
    }

    private Optional<IMethod> resolveSelectedMethodName(IEditorPart editor, ITypeRoot element) {
        try {
            ISelectionProvider selectionProvider = editor.getSite().getSelectionProvider();
            if (selectionProvider == null) {
                return Optional.absent();
            }

            ISelection selection = selectionProvider.getSelection();
            if (!(selection instanceof ITextSelection)) {
                return Optional.absent();
            }

            ITextSelection textSelection = (ITextSelection) selection;

            IJavaElement elementAtOffset = SelectionConverter.getElementAtOffset(element, textSelection);
            if (elementAtOffset instanceof IMethod) {
                return Optional.of((IMethod) elementAtOffset);
            }

        } catch (JavaModelException e) {
            UiPlugin.logger().info("The method name for running tests cannot be determined", e); //$NON-NLS-1$
        }
        return Optional.absent();
    }

    private void logNoTestsFound() {
        CorePlugin.userNotification().errorOccurred(LaunchMessages.Test_Not_Found_Dialog_Title,
                LaunchMessages.Test_Not_Found_Dialog_Message, LaunchMessages.Test_Not_Found_Dialog_Details,
                IStatus.WARNING, null);
    }
}
