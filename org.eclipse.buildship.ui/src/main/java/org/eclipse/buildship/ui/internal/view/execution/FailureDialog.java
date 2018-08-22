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

package org.eclipse.buildship.ui.internal.view.execution;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import org.eclipse.buildship.core.internal.GradlePluginsRuntimeException;
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
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.gradle.tooling.Failure;
import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.FinishEvent;

import java.net.URI;
import java.util.List;

/**
 * Dialog presenting a list of {@link Failure} instances.
 */
public final class FailureDialog extends Dialog {

    private static final String FAILURE_DETAILS_URL_PREFIX = "org.gradle.api.GradleException: There were failing tests. See the report at: "; //$NON-NLS-1$

    private final String title;
    private final ImmutableList<FailureItem> failureItems;

    private Label operationNameText;
    private Text messageText;
    private Text detailsText;
    private Label urlLabel;
    private Link urlLink;
    private Button backButton;
    private Button nextButton;
    private Button copyButton;
    private Clipboard clipboard;

    private int selectionIndex;

    public FailureDialog(Shell parent, String title, List<FinishEvent> failureEvents) {
        super(parent);
        this.title = Preconditions.checkNotNull(title);
        this.failureItems = FailureItem.from(failureEvents);
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
        operationNameLabel.setText(ExecutionViewMessages.Dialog_Failure_Operation_Label);

        this.operationNameText = new Label(container, SWT.NONE);
        GridData operationNameLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        this.operationNameText.setLayoutData(operationNameLayoutData);

        this.backButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.backButton.setToolTipText(ExecutionViewMessages.Dialog_Failure_Back_Tooltip);
        this.backButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.backButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));

        this.nextButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.nextButton.setToolTipText(ExecutionViewMessages.Dialog_Failure_Next_Tooltip);
        this.nextButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.nextButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));

        this.copyButton = new Button(container, SWT.FLAT | SWT.CENTER);
        this.copyButton.setToolTipText(ExecutionViewMessages.Dialog_Failure_Copy_Details_Tooltip);
        this.copyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        this.copyButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));

        Label messageLabel = new Label(container, SWT.NONE);
        messageLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        messageLabel.setText(ExecutionViewMessages.Dialog_Failure_Message_Label);

        this.messageText = new Text(container, SWT.BORDER);
        GridData messageTextLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
        this.messageText.setLayoutData(messageTextLayoutData);
        this.messageText.setEditable(false);

        Label detailsLabel = new Label(container, SWT.NONE);
        detailsLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        detailsLabel.setText(ExecutionViewMessages.Dialog_Failure_Details_Label);

        this.detailsText = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData detailsTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
        detailsTextGridData.heightHint = 200;
        this.detailsText.setLayoutData(detailsTextGridData);
        this.detailsText.setEditable(false);

        this.urlLabel = new Label(container, SWT.NONE);
        this.urlLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        this.urlLabel.setText(ExecutionViewMessages.Dialog_Failure_Link_Label);

        this.urlLink = new Link(container, SWT.NONE);
        this.urlLink.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));

        this.clipboard = new Clipboard(parent.getDisplay());

        initSelectionIndex();
        initEventListeners();
        update();

        return container;
    }

    private void initSelectionIndex() {
        this.selectionIndex = this.failureItems.isEmpty() ? -1 : 0;
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

        this.urlLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                String url = (String) FailureDialog.this.urlLink.getData();
                try {
                    // if there is a browser with the same url then reuse it, otherwise open a new one
                    IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
                    IWebBrowser browser = browserSupport.createBrowser(IWorkbenchBrowserSupport.AS_EDITOR, url, null, url);
                    browser.openURL(URI.create(url).toURL());
                    close();
                } catch (Exception e) {
                    throw new GradlePluginsRuntimeException(String.format("Cannot open browser editor for %s.", url), e);
                }
            }
        });
    }

    @SuppressWarnings("RedundantTypeArguments")
    private void update() {
        Optional<FailureItem> failureItem = this.selectionIndex == -1 ? Optional.<FailureItem>absent() : Optional.of(this.failureItems.get(this.selectionIndex));
        Optional<Failure> failure = failureItem.isPresent() ? failureItem.get().failure : Optional.<Failure>absent();

        this.operationNameText.setText(failureItem.isPresent() ? ExecutionPageNameLabelProvider.renderVerbose(failureItem.get().event) : ""); //$NON-NLS-1$

        this.messageText.setText(failure.isPresent() ? Strings.nullToEmpty(failure.get().getMessage()) : ""); //$NON-NLS-1$
        this.messageText.setEnabled(failureItem.isPresent());

        this.detailsText.setText(failure.isPresent() ? collectDetails(failure.get()) : ""); //$NON-NLS-1$
        this.detailsText.setEnabled(failureItem.isPresent());

        this.backButton.setEnabled(this.selectionIndex > 0);
        this.nextButton.setEnabled(this.selectionIndex < this.failureItems.size() - 1);
        this.copyButton.setEnabled(failureItem.isPresent() && failure.isPresent() && failure.get().getDescription() != null);

        Optional<String> testReportUrl = findTestReportUrl(failure);
        this.urlLabel.setVisible(testReportUrl.isPresent());
        this.urlLink.setVisible(testReportUrl.isPresent());
        this.urlLink.setText(testReportUrl.isPresent() ? "<a>Test Summary</a>" : "");
        this.urlLink.setData(testReportUrl.isPresent() ? testReportUrl.get() : null);

        // force redraw since different failures can have different number of lines in the message
        this.operationNameText.getParent().layout(true);
    }

    private Optional<String> findTestReportUrl(Optional<Failure> failure) {
        if (failure.isPresent()) {
            String description = failure.get().getDescription();
            int beginIndex = description.indexOf(FAILURE_DETAILS_URL_PREFIX);
            if (beginIndex >= 0) {
                int endIndex = description.indexOf('\n', beginIndex);
                String url = description.substring(beginIndex + FAILURE_DETAILS_URL_PREFIX.length(), endIndex);
                return Optional.of(url);
            }
        }
        return Optional.absent();
    }

    private String collectDetails(Failure failure) {
        return collectDetailsRecursively(failure);
    }

    private String collectDetailsRecursively(Failure failure) {
        StringBuilder result = new StringBuilder();
        result.append(Strings.nullToEmpty(failure.getDescription()));
        List<? extends Failure> causes = failure.getCauses();
        if (!causes.isEmpty()) {
            result.append('\n').append(ExecutionViewMessages.Dialog_Failure_Root_Cause_Label).append(' ');
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

    /**
     * Represents a failure item shown in the failure dialog. One finish event can have multiple
     * failures and so for each failure of each event we show a failure item in the failure dialog.
     */
    private static final class FailureItem {

        private final FinishEvent event;
        private final Optional<Failure> failure;

        private FailureItem(FinishEvent event, Optional<Failure> failure) {
            this.event = event;
            this.failure = failure;
        }

        private static ImmutableList<FailureItem> from(final FinishEvent event) {
            List<? extends Failure> failures = ((FailureResult) event.getResult()).getFailures();
            ImmutableList<FailureItem> failureItems = FluentIterable.from(failures).transform(new Function<Failure, FailureItem>() {

                @Override
                public FailureItem apply(Failure failure) {
                    return new FailureItem(event, Optional.of(failure));
                }
            }).toList();
            return failureItems.isEmpty() ? ImmutableList.of(new FailureItem(event, Optional.<Failure>absent())) : failureItems;
        }

        private static ImmutableList<FailureItem> from(List<FinishEvent> events) {
            ImmutableList.Builder<FailureItem> failureItems = ImmutableList.builder();
            for (FinishEvent event : events) {
                failureItems.addAll(from(event));
            }
            return failureItems.build();
        }

    }

}
