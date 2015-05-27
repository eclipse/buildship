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

import java.util.List;

import org.gradle.tooling.Failure;
import org.gradle.tooling.events.FailureResult;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog presenting a list of {@link Failure} instances.
 */
public final class FailureDialog extends Dialog {

    private final String title;
    private final List<Failure> failures;

    private Text messageText;
    private Button backButton;
    private Button nextButton;
    private Button copyButton;
    private Text stacktraceText;
    private Clipboard clipboard;

    private int selectedFailure = 0;

    public FailureDialog(Shell parent, String title, FailureResult failure) {
        super(parent);
        this.title = Preconditions.checkNotNull(title);
        this.failures = ImmutableList.copyOf(failure.getFailures());
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

        Label messageLabel = new Label(container, SWT.NONE);
        messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        messageLabel.setText(ExecutionsViewMessages.Dialog_Failure_Message_Label);

        this.messageText = new Text(container, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
        this.messageText.setEditable(false);
        GridData messageTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        messageTextGridData.heightHint = convertVerticalDLUsToPixels(10);
        this.messageText.setLayoutData(messageTextGridData);

        this.backButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.backButton.setToolTipText(ExecutionsViewMessages.Dialog_Failure_Back_Tooltip);
        this.backButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

        this.nextButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.nextButton.setToolTipText(ExecutionsViewMessages.Dialog_Failure_Next_Tooltip);
        this.nextButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

        this.copyButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.copyButton.setToolTipText(ExecutionsViewMessages.Dialog_Failure_Copy_Stacktrace_Tooltip);
        this.copyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.copyButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));

        this.stacktraceText = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.stacktraceText.setEditable(false);
        GridData stacktraceTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 5, 1);
        stacktraceTextGridData.heightHint = 200;
        this.stacktraceText.setLayoutData(stacktraceTextGridData);

        this.clipboard = new Clipboard(parent.getDisplay());

        initEventListeners();
        initImagesAndEnablement();
        updateContent();

        return container;
    }

    private void initEventListeners() {
        this.backButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FailureDialog.this.selectedFailure--;
                if (FailureDialog.this.selectedFailure < 0) {
                    FailureDialog.this.selectedFailure += FailureDialog.this.failures.size();
                }
                updateContent();
            }
        });

        this.nextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FailureDialog.this.selectedFailure = (FailureDialog.this.selectedFailure + 1) % FailureDialog.this.failures.size();
                updateContent();
            }
        });
        this.copyButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FailureDialog.this.clipboard.setContents(new Object[] { FailureDialog.this.stacktraceText.getText() }, new Transfer[] { TextTransfer.getInstance() });
            }
        });
    }

    private void initImagesAndEnablement() {
        if (this.failures.size() == 0) {
            this.backButton.setEnabled(false);
            this.nextButton.setEnabled(false);
            this.backButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK_DISABLED));
            this.nextButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
        } else if (this.failures.size() == 1) {
            this.backButton.setEnabled(false);
            this.nextButton.setEnabled(false);
            this.backButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK_DISABLED));
            this.nextButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD_DISABLED));
        } else {
            this.backButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
            this.nextButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
        }
    }

    private void updateContent() {
        // update the message and the stacktrace texts
        this.messageText.setText(collectFailureMessage());
        this.stacktraceText.setText(collectFailureDescription());
        // disable copy button if there is nothing to copy
        Optional<Failure> currentFailure = getCurrentFailure();
        this.copyButton.setEnabled(currentFailure.isPresent() && currentFailure.get().getDescription() != null);
    }

    private String collectFailureMessage() {
        Optional<Failure> failure = getCurrentFailure();
        if (failure.isPresent()) {
            return MoreObjects.firstNonNull(failure.get().getMessage(), ""); //$NON-NLS-1$
        } else {
            return ExecutionsViewMessages.Dialog_Failure_No_Stacktrace_Message_Label;
        }
    }

    private String collectFailureDescription() {
        return collectFailureDescriptionsRecursively(getCurrentFailure());
    }

    private String collectFailureDescriptionsRecursively(Optional<Failure> failure) {
        if (failure.isPresent()) {
            StringBuilder result = new StringBuilder();
            result.append(MoreObjects.firstNonNull(failure.get().getDescription(), "")); //$NON-NLS-1$
            List<? extends Failure> causes = failure.get().getCauses();
            if (!causes.isEmpty()) {
                result.append(ExecutionsViewMessages.Dialog_Failure_Root_Cause_Label);
                for (Failure cause : causes) {
                    result.append(collectFailureDescriptionsRecursively(Optional.of(cause)));
                }
            }
            return result.toString();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    private Optional<Failure> getCurrentFailure() {
        if (this.selectedFailure < 0 || this.selectedFailure >= this.failures.size()) {
            return Optional.absent();
        } else {
            return Optional.<Failure> of(this.failures.get(this.selectedFailure));
        }
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
