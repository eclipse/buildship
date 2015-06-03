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

package org.eclipse.buildship.ui.view.execution;

import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.ui.generic.NodeSelection;
import org.eclipse.buildship.ui.generic.SelectionSpecificAction;

/**
 * Reruns the tests selected in the target {@code ExecutionPage}.
 */
public final class RerunTestsAction extends Action implements SelectionSpecificAction {

    private final ExecutionPage executionPage;

    public RerunTestsAction(ExecutionPage executionPage) {
        this.executionPage = Preconditions.checkNotNull(executionPage);

        setText(ExecutionsViewMessages.Action_RerunTests_Text);
    }

    @Override
    public void run() {
        // get the run configuration which triggered the execution page
        GradleRunConfigurationAttributes configuration = this.executionPage.getConfigurationAttributes();

        // append extra arguments to the existing list to rerun the tests
        ImmutableList<String> newArguments = argumentsForReRunningPreviousTests(configuration);

        // create a new, updated launch config with the new arguments and launch it
        GradleRunConfigurationAttributes newConfiguration = GradleRunConfigurationAttributes
                .with(configuration.getTasks(), configuration.getWorkingDirExpression(), configuration.getGradleDistribution(), configuration.getGradleUserHomeExpression(), configuration
                        .getJavaHomeExpression(), configuration.getJvmArgumentExpressions(), newArguments, configuration.isShowExecutionView(), configuration
                        .isShowConsoleView());
        ILaunchConfiguration launchConfiguration = CorePlugin.gradleLaunchConfigurationManager().getOrCreateRunConfiguration(newConfiguration);
        DebugUITools.launch(launchConfiguration, ILaunchManager.RUN_MODE);
    }

    private ImmutableList<String> argumentsForReRunningPreviousTests(GradleRunConfigurationAttributes configuration) {
        Builder<String> newArguments = ImmutableList.<String> builder().addAll(configuration.getArguments());
        for (JvmTestOperationDescriptor event : collectTestOperations(this.executionPage.getSelection())) {
            String className = event.getClassName();
            if (className != null) {
                newArguments.add("--tests"); //$NON-NLS-1$
                newArguments.add(event.getClassName());
            }
        }
        return newArguments.build();
    }

    private ImmutableList<JvmTestOperationDescriptor> collectTestOperations(NodeSelection selection) {
        // retrieve the JvMOperationDescriptor instances from the selected nodes
        return FluentIterable.from(selection.getNodes(OperationItem.class)).filter(new Predicate<OperationItem>() {

            @Override
            public boolean apply(OperationItem operationItem) {
                OperationDescriptor descriptor = (OperationDescriptor) operationItem.getAdapter(OperationDescriptor.class);
                return descriptor instanceof JvmTestOperationDescriptor;
            }
        }).transform(new Function<OperationItem, JvmTestOperationDescriptor>() {

            @Override
            public JvmTestOperationDescriptor apply(OperationItem operationItem) {
                return (JvmTestOperationDescriptor) operationItem.getAdapter(OperationDescriptor.class);
            }
        }).toList();
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return !collectTestOperations(selection).isEmpty();
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        return !collectTestOperations(selection).isEmpty();
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

}
