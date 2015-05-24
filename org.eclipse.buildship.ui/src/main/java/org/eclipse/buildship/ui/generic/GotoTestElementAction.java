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

package org.eclipse.buildship.ui.generic;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.view.execution.OperationItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Display;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.test.JvmTestOperationDescriptor;

/**
 * This actions runs the {@link OpenTestCompilationUnitJob}, which navigates to the source of a test
 * according to the ISelection.
 */
public final class GotoTestElementAction extends Action implements SelectionSpecificAction {

    private ISelectionProvider selectionProvider;
    private Display display;

    public GotoTestElementAction(ISelectionProvider selectionProvider, Display display) {
        super(UiMessages.Action_OpenTestSourceFile_Text);
        this.selectionProvider = selectionProvider;
        this.display = display;
    }

    @Override
    public void run() {
        ISelection selection = this.selectionProvider.getSelection();
        OpenTestCompilationUnitJob openTestCompilationUnitJob = new OpenTestCompilationUnitJob(selection, this.display);
        openTestCompilationUnitJob.schedule();
    }

    @Override
    public boolean isVisibleFor(NodeSelection selection) {
        return isEnabledFor(selection);
    }

    @Override
    public boolean isEnabledFor(NodeSelection selection) {
        if (selection.isEmpty()) {
            return false;
        }

        if (!selection.hasAllNodesOfType(OperationItem.class)) {
            return false;
        }

        ImmutableList<OperationItem> operationItems = selection.getNodes(OperationItem.class);
        return FluentIterable.from(operationItems).allMatch(new Predicate<OperationItem>() {
            @Override
            public boolean apply(OperationItem operationItem) {
                OperationDescriptor adapter = (OperationDescriptor) operationItem.getAdapter(OperationDescriptor.class);
                return adapter instanceof JvmTestOperationDescriptor && ((JvmTestOperationDescriptor) adapter).getClassName() != null;
            }
        });
    }

    @Override
    public void setEnabledFor(NodeSelection selection) {
        setEnabled(isEnabledFor(selection));
    }

}
