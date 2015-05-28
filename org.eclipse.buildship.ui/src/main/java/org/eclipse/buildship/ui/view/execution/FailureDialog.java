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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.gradle.tooling.Failure;
import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.FinishEvent;

import java.util.List;

/**
 * Dialog presenting a list of {@link Failure} instances.
 */
public final class FailureDialog extends Dialog {

    private final String title;
    private final ImmutableList<FinishEvent> failureEvents;

    private Label operationNameText;
    private Text messageText;
    private Text detailsText;
    private Button backButton;
    private Button nextButton;
    private Button copyButton;
    private Clipboard clipboard;

    private int selectionIndex;

    public FailureDialog(Shell parent, String title, List<FinishEvent> failureEvents) {
        super(parent);
        this.title = Preconditions.checkNotNull(title);
        this.failureEvents = ImmutableList.copyOf(failureEvents);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(this.title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridData containerGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        containerGridData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        container.setLayoutData(containerGridData);
        container.setLayout(new GridLayout(5, false));

        Label operationNameLabel = new Label(container, SWT.NONE);
        operationNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        operationNameLabel.setText(ExecutionsViewMessages.Dialog_Failure_Operation_Label);

        this.operationNameText = new Label(container, SWT.BORDER);
        GridData operationNameLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        operationNameLayoutData.heightHint = convertVerticalDLUsToPixels(10);
        this.operationNameText.setLayoutData(operationNameLayoutData);

        this.backButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.backButton.setToolTipText(ExecutionsViewMessages.Dialog_Failure_Back_Tooltip);
        this.backButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.backButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));

        this.nextButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.nextButton.setToolTipText(ExecutionsViewMessages.Dialog_Failure_Next_Tooltip);
        this.nextButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.nextButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));

        this.copyButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.copyButton.setToolTipText(ExecutionsViewMessages.Dialog_Failure_Copy_Details_Tooltip);
        this.copyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.copyButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));

        Label messageLabel = new Label(container, SWT.NONE);
        messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        messageLabel.setText(ExecutionsViewMessages.Dialog_Failure_Message_Label);

        this.messageText = new Text(container, SWT.BORDER);
        GridData messageTextLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
        this.messageText.setLayoutData(messageTextLayoutData);
        this.messageText.setEditable(false);

        Label detailsLabel = new Label(container, SWT.NONE);
        detailsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        detailsLabel.setText(ExecutionsViewMessages.Dialog_Failure_Details_Label);

        this.detailsText = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData detailsTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        detailsTextGridData.heightHint = 200;
        this.detailsText.setLayoutData(detailsTextGridData);
        this.detailsText.setEditable(false);

        this.clipboard = new Clipboard(parent.getDisplay());

        initSelectionIndex();
        initEventListeners();
        update();

        return container;
    }

    private void initSelectionIndex() {
        this.selectionIndex = this.failureEvents.isEmpty() ? -1 : 0;
    }

    private void initEventListeners() {
        this.backButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FailureDialog.this.selectionIndex--;
                update();
            }
        });

        this.nextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FailureDialog.this.selectionIndex++;
                update();
            }
        });

        this.copyButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object[] data = {FailureDialog.this.detailsText.getText()};
                Transfer[] dataTypes = {TextTransfer.getInstance()};
                FailureDialog.this.clipboard.setContents(data, dataTypes);
            }
        });
    }

    @SuppressWarnings("RedundantTypeArguments")
    private void update() {
        Optional<FinishEvent> failureEvent = this.selectionIndex == -1 ? Optional.<FinishEvent>absent() : Optional.of(this.failureEvents.get(this.selectionIndex));
        List<? extends Failure> failures = failureEvent.isPresent() ? ((FailureResult) failureEvent.get().getResult()).getFailures() : ImmutableList.<Failure>of();
        Optional<Failure> failure = failures.isEmpty() ? Optional.<Failure>absent() : Optional.<Failure>of(failures.get(0));

        this.operationNameText.setText(failureEvent.isPresent() ? OperationDescriptorRenderer.renderVerbose(failureEvent.get()) : "");

        this.messageText.setText(failure.isPresent() ? Strings.nullToEmpty(failure.get().getMessage()) : "");
        this.messageText.setEnabled(failureEvent.isPresent());

        this.detailsText.setText(failure.isPresent() ? collectDetails(failure.get()) : "");
        this.detailsText.setEnabled(failureEvent.isPresent());

        this.backButton.setEnabled(this.selectionIndex > 0);
        this.nextButton.setEnabled(this.selectionIndex < this.failureEvents.size() - 1);
        this.copyButton.setEnabled(failure.isPresent() && failure.get().getDescription() != null);

        // force redraw since different failures can have different number of lines in the message
        this.operationNameText.getParent().layout(true);
    }

    private String collectDetails(Failure failure) {
        return collectDetailsRecursively(failure);
    }

    private String collectDetailsRecursively(Failure failure) {
        StringBuilder result = new StringBuilder();
        result.append(Strings.nullToEmpty(failure.getDescription()));
        List<? extends Failure> causes = failure.getCauses();
        if (!causes.isEmpty()) {
            result.append(ExecutionsViewMessages.Dialog_Failure_Root_Cause_Label);
            for (Failure cause : causes) {
                result.append(collectDetailsRecursively(cause));
            }
        }
        return result.toString();
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
    }

    @Override
    public boolean close() {
        if (this.clipboard != null) {
            this.clipboard.dispose();
            this.clipboard = null;
        }
        return super.close();
    }

}
