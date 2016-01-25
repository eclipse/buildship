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

package org.eclipse.buildship.ui.notification;

import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.List;

/**
 * Custom {@link Dialog} implementation showing one or more exceptions.
 */
public final class ExceptionDetailsDialog extends Dialog {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final int COPY_EXCEPTION_BUTTON_ID = 25;

    private final String title;
    private final String message;
    private final String details;
    private final ArrayList<Throwable> throwables;

    private Image image;
    private Button detailsButton;
    private Composite stackTraceAreaControl;
    private Label singleErrorMessageLabel;
    private TableViewer exceptionsViewer;
    private Label singleErrorDetailsLabel;
    private Label multiErrorMessageLabel;
    private Clipboard clipboard;
    private Text stacktraceAreaText;
    private Composite singleErrorContainer;
    private Composite multiErrorContainer;
    private StackLayout stackLayout;

    public ExceptionDetailsDialog(Shell shell, String title, String message, String details, int severity, Throwable throwable) {
        super(new SameShellProvider(shell));
        this.title = Preconditions.checkNotNull(title);
        this.message = Preconditions.checkNotNull(message);
        this.details = Preconditions.checkNotNull(details);
        this.throwables = new ArrayList<Throwable>(Arrays.asList(throwable));
        this.image = getIconForSeverity(severity, shell);
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        dialogArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        // dialog image
        ((GridLayout) dialogArea.getLayout()).numColumns = 2;
        Label imageLabel = new Label(dialogArea, 0);
        this.image.setBackground(imageLabel.getBackground());
        imageLabel.setImage(this.image);
        imageLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING));

        // composite to include all text widgets
        Composite textArea = new Composite(dialogArea, SWT.NONE);
        GridLayout textAreaLayout = new GridLayout(1, false);
        textAreaLayout.verticalSpacing = FontUtils.getFontHeightInPixels(parent.getFont());
        textAreaLayout.marginWidth = textAreaLayout.marginHeight = 0;
        textArea.setLayout(textAreaLayout);
        GridData textAreaLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        textAreaLayoutData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        textArea.setLayoutData(textAreaLayoutData);

        Composite stackLayoutContainer = new Composite(textArea, SWT.NONE);
        this.stackLayout = new StackLayout();
        stackLayoutContainer.setLayout(this.stackLayout);
        stackLayoutContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

        // single error container
        this.singleErrorContainer = new Composite(stackLayoutContainer, SWT.NONE);
        GridLayout singleErrorContainerLayout = new GridLayout(1, false);
        singleErrorContainerLayout.marginWidth = singleErrorContainerLayout.marginHeight = 0;
        this.singleErrorContainer.setLayout(singleErrorContainerLayout);
        this.stackLayout.topControl = this.singleErrorContainer;

        // single error label
        this.singleErrorMessageLabel = new Label(this.singleErrorContainer, SWT.WRAP);
        GridData messageLabelGridData = new GridData();
        messageLabelGridData.verticalAlignment = SWT.TOP;
        messageLabelGridData.grabExcessHorizontalSpace = true;
        this.singleErrorMessageLabel.setLayoutData(messageLabelGridData);
        this.singleErrorMessageLabel.setText(this.message);

        // single error details
        this.singleErrorDetailsLabel = new Label(this.singleErrorContainer, SWT.WRAP);
        GridData detailsLabelGridData = new GridData();
        detailsLabelGridData.verticalAlignment = SWT.TOP;
        detailsLabelGridData.grabExcessHorizontalSpace = true;
        this.singleErrorDetailsLabel.setLayoutData(detailsLabelGridData);
        this.singleErrorDetailsLabel.setText(this.details);

        // multi error container
        this.multiErrorContainer = new Composite(stackLayoutContainer, SWT.NONE);
        GridLayout multiErrorContainerLayout = new GridLayout(1, false);
        multiErrorContainerLayout.marginWidth = multiErrorContainerLayout.marginHeight = 0;
        this.multiErrorContainer.setLayout(multiErrorContainerLayout);

        // multi error label
        this.multiErrorMessageLabel = new Label(this.multiErrorContainer, SWT.WRAP);
        this.multiErrorMessageLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        this.multiErrorMessageLabel.setText(this.message);

        // multi error messages displayed in a list viewer
        GridData multiErrorExceptionListGridData = new GridData();
        multiErrorExceptionListGridData.horizontalAlignment = SWT.FILL;
        multiErrorExceptionListGridData.verticalAlignment = SWT.FILL;
        multiErrorExceptionListGridData.grabExcessHorizontalSpace = true;
        multiErrorExceptionListGridData.grabExcessVerticalSpace = true;
        multiErrorExceptionListGridData.widthHint = 800;
        this.exceptionsViewer = new TableViewer(this.multiErrorContainer, SWT.MULTI);
        this.exceptionsViewer.getControl().setLayoutData(multiErrorExceptionListGridData);
        this.exceptionsViewer.setContentProvider(new ArrayContentProvider());
        this.exceptionsViewer.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof Throwable) {
                    return ((Throwable) element).getMessage();
                } else {
                    return "";
                }
            }
        });
        this.exceptionsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                updateStacktraceArea();
            }
        });

        // set clipboard
        this.clipboard = new Clipboard(getShell().getDisplay());

        // update
        updateDisplayedExceptions();

        return dialogArea;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button copyExceptionButton = createButton(parent, COPY_EXCEPTION_BUTTON_ID, "", false);
        copyExceptionButton.setToolTipText(UiMessages.Button_CopyFailuresToClipboard_Tooltip);

        copyExceptionButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
        this.detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL, false);
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setFocus();
    }

    @Override
    protected void setButtonLayoutData(Button button) {
        if (button.getData() != null && button.getData().equals(COPY_EXCEPTION_BUTTON_ID)) {
            // do not set a width hint for the copy error button, like it is done in the super
            // implementation
            GridDataFactory.swtDefaults().applyTo(button);
            return;
        }
        super.setButtonLayoutData(button);
    }

    @Override
    protected void initializeBounds() {
        // do not make columns equal width so that we can have a smaller 'copy failure' button
        Composite buttonBar = (Composite) getButtonBar();
        GridLayout layout = (GridLayout) buttonBar.getLayout();
        layout.makeColumnsEqualWidth = false;
        super.initializeBounds();
    }

    @Override
    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            toggleStacktraceArea();
        } else if (id == COPY_EXCEPTION_BUTTON_ID) {
            copyStacktracesToClipboard();
        } else {
            super.buttonPressed(id);
        }
    }

    @Override
    public boolean close() {
        if (this.clipboard != null) {
            this.clipboard.dispose();
            this.clipboard = null;
        }
        return super.close();
    }

    private Image getIconForSeverity(int severity, Shell shell) {
        int swtImageKey;
        switch (severity) {
            case IStatus.OK:
            case IStatus.INFO:
                swtImageKey = SWT.ICON_INFORMATION;
                break;
            case IStatus.WARNING:
            case IStatus.CANCEL:
                swtImageKey = SWT.ICON_WARNING;
                break;
            case IStatus.ERROR:
                swtImageKey = SWT.ICON_ERROR;
                break;
            default:
                // for unknown severity display the warning image
                swtImageKey = SWT.ICON_WARNING;
                UiPlugin.logger().warn("Can't find image for severity: " + severity);
        }

        return shell.getDisplay().getSystemImage(swtImageKey);
    }

    /**
     * Adds a new exception to the dialog. If the dialog is not yet visible the exception is stored
     * and will be displayed after the client calls {@link #open()}.
     *
     * @param throwable the exception to show
     */
    public void addException(Throwable throwable) {
        final Throwable exception = throwable;
        // save the new exceptions
        this.throwables.add(exception);

        // update the UI elements if the dialog was already opened
        if (getContents() != null) {
            updateDisplayedExceptions();
        }
    }

    private void updateDisplayedExceptions() {
        // set the input for the exception list
        setExceptionsViewerInput(this.throwables);

        if (this.throwables.size() > 1) {
            setDialogTitle(UiMessages.Dialog_Title_Multiple_Errors);
            showMultiError();
        } else {
            setDialogTitle(this.title);
            showSingleError();
        }
    }

    private void toggleStacktraceArea() {
        if (isStacktraceAreaVisible()) {
            hideStacktraceArea();
        } else {
            showStacktraceArea();
            updateStacktraceArea();
        }
        relayoutShell();
    }

    private void relayoutShell() {
        // compute the new window size
        Point oldSize = getContents().getSize();
        Point newSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);

        Point oldWindowSize = getShell().getSize();
        Point newWindowSize = new Point(oldWindowSize.x, oldWindowSize.y + (newSize.y - oldSize.y));

        // crop new window size to screen
        Point windowLocation = getShell().getLocation();
        Rectangle screenArea = getContents().getDisplay().getClientArea();
        if (newWindowSize.y > screenArea.height - (windowLocation.y - screenArea.y)) {
            newWindowSize.y = screenArea.height - (windowLocation.y - screenArea.y);
        }

        getShell().setSize(newWindowSize);
        ((Composite) getContents()).layout();
    }

    private void updateStacktraceArea() {
        // show only the selected exceptions in the dialog area or all of them if nothing
        // is selected
        Collection<Throwable> selectedExceptions = getSelectedExceptionsFromViewer();
        if (selectedExceptions.isEmpty()) {
            selectedExceptions = this.throwables;
        }
        setStacktraceAreaText(collectStackTraces(selectedExceptions));
    }

    private boolean isStacktraceAreaVisible() {
        return this.stackTraceAreaControl != null;
    }

    private void showStacktraceArea() {
        // create the stacktrace container area
        this.stackTraceAreaControl = new Composite((Composite) getContents(), SWT.NONE);
        this.stackTraceAreaControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout containerLayout = new GridLayout();
        containerLayout.marginHeight = containerLayout.marginWidth = 0;
        this.stackTraceAreaControl.setLayout(containerLayout);

        // the text inside the stacktrace area
        this.stacktraceAreaText = new Text(this.stackTraceAreaControl, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.stacktraceAreaText.setLayoutData(new GridData(GridData.FILL_BOTH));

        // update button
        this.detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
    }

    private String collectStackTraces(Collection<Throwable> throwables) {
        Writer writer = new StringWriter(1024);
        PrintWriter printWriter = new PrintWriter(writer);
        for (Throwable throwable : throwables) {
            throwable.printStackTrace(printWriter);
            printWriter.write(LINE_SEPARATOR);
        }
        return writer.toString();
    }

    private void copyStacktracesToClipboard() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.message);
        sb.append(LINE_SEPARATOR);
        sb.append(this.details);
        sb.append(LINE_SEPARATOR);
        sb.append(collectStackTraces(this.throwables));
        setClipboardContent(sb.toString());
    }

    private void hideStacktraceArea() {
        this.stackTraceAreaControl.dispose();
        this.stackTraceAreaControl = null;
        this.detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
    }

    private void showSingleError() {
        if (this.stackLayout != null && isAccessible(this.singleErrorContainer)) {
            this.stackLayout.topControl = this.singleErrorContainer;
            this.singleErrorContainer.getParent().layout();
        }
    }

    private void showMultiError() {
        if (this.stackLayout != null && isAccessible(this.multiErrorContainer)) {
            this.stackLayout.topControl = this.multiErrorContainer;
            this.multiErrorContainer.getParent().layout();
        }
    }

    private Collection<Throwable> getSelectedExceptionsFromViewer() {
        if (isAccessible(this.exceptionsViewer)) {
            ISelection selection = this.exceptionsViewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                @SuppressWarnings("unchecked")
                List<Object> structuredSelection = ((IStructuredSelection) selection).toList();
                return FluentIterable.<Object>from(structuredSelection).filter(Throwable.class).toList();
            }
        }
        // if nothing is selected then return all available exceptions
        return Collections.emptyList();
    }

    private void setClipboardContent(String content) {
        if (this.clipboard != null && !this.clipboard.isDisposed()) {
            this.clipboard.setContents(new String[] { content }, new Transfer[] { TextTransfer.getInstance() });
        }
    }

    private void setExceptionsViewerInput(Collection<Throwable> input) {
        if (isAccessible(this.exceptionsViewer)) {
            this.exceptionsViewer.setInput(input);
        }
    }

    private void setStacktraceAreaText(String text) {
        if (isAccessible(this.stacktraceAreaText)) {
            this.stacktraceAreaText.setText(text);
        }
    }

    private void setDialogTitle(String title) {
        Shell control = getShell();
        if (isAccessible(control)) {
            control.setText(title);
        }
    }

    private static boolean isAccessible(Widget widget) {
        return widget != null && !widget.isDisposed();
    }

    private static boolean isAccessible(Viewer widget) {
        return widget != null && isAccessible(widget.getControl());
    }

}
