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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import org.eclipse.buildship.core.GradlePluginsRuntimeException;
import org.eclipse.buildship.ui.i18n.UiMessages;
import org.eclipse.buildship.ui.util.font.FontUtils;
import org.eclipse.buildship.ui.util.test.SWTBotWidgetIdentifierKey;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Custom {@link Dialog} implementation showing an exception and its stacktrace.
 */
public final class ExceptionDetailsDialog extends Dialog {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final int COPY_EXCEPTION_BUTTON_ID = 25;

    private final Image image;
    private final String title;
    private final String message;
    private final String details;
    private final IObservableList throwables;

    private Button detailsButton;
    private Control stackTraceArea;

    private Clipboard clipboard;
    private Text detailText;
    private StructuredViewer errorViewer;

    public ExceptionDetailsDialog(Shell shell, String title, String message, String details, int severity,
            Throwable... throwable) {
        super(new SameShellProvider(shell));

        this.image = getIconForSeverity(severity, shell);
        this.title = Preconditions.checkNotNull(title);
        this.message = Preconditions.checkNotNull(message);
        this.details = Preconditions.checkNotNull(details);

        this.throwables = Properties.selfList(Throwable.class)
                .observe(Lists.newArrayList(Preconditions.checkNotNull(throwable)));

        this.throwables.addListChangeListener(new IListChangeListener() {

            @Override
            public void handleListChange(ListChangeEvent event) {

                if (isMultiErrorDialog()) {
                    if (ExceptionDetailsDialog.this.errorViewer == null
                            || ExceptionDetailsDialog.this.errorViewer.getControl() == null
                            || ExceptionDetailsDialog.this.errorViewer.getControl().isDisposed()) {
                        // dispose the contents of the single error dialog ...
                        Composite dialogAreaComposite = (Composite) ExceptionDetailsDialog.this.dialogArea;
                        Control[] children = dialogAreaComposite.getChildren();
                        for (Control control : children) {
                            control.dispose();
                        }
                        // ... and create the MultiErrorDialog
                        createMultiErrorDialog(dialogAreaComposite);
                        relayoutShell();
                    }
                }

            }
        });

        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
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
            throw new GradlePluginsRuntimeException("Can't find image for severity: " + severity); //$NON-NLS-1$
        }

        return shell.getDisplay().getSystemImage(swtImageKey);
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        // set dialog box title
        shell.setText(this.title);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        ((GridLayout) container.getLayout()).numColumns = 2;
        container.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        if (isMultiErrorDialog()) {
            createMultiErrorDialog(container);
        } else {
            createSingleErrorDialog(container);
        }

        this.clipboard = new Clipboard(getShell().getDisplay());

        return container;
    }

    private void createMultiErrorDialog(Composite container) {

        // change the title of the shell
        getShell().setText(UiMessages.Dialog_Title_Multiple_Error);

        // dialog image
        Label imageLabel = new Label(container, 0);
        this.image.setBackground(imageLabel.getBackground());
        imageLabel.setImage(this.image);
        imageLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING));

        // message label
        Label messageLabel = new Label(container, SWT.WRAP);
        messageLabel.setText(this.message);
        GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(messageLabel);

        this.errorViewer = new TableViewer(container);
        this.errorViewer.getControl().setData(SWTBotWidgetIdentifierKey.DEFAULT_KEY,
                ExceptionDetailsDialog.class.getName() + "TableViewer:errorViewer"); //$NON-NLS-1$
        GridDataFactory.fillDefaults().grab(true, true).hint(SWT.DEFAULT, 80).span(2, 1)
                .applyTo(this.errorViewer.getControl());
        ViewerSupport.bind(this.errorViewer, this.throwables, PojoProperties.value("message")); //$NON-NLS-1$
        this.errorViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                if (ExceptionDetailsDialog.this.detailText != null
                        && !ExceptionDetailsDialog.this.detailText.isDisposed()) {
                    ExceptionDetailsDialog.this.detailText.setText(getStackTrace(getSelectedThrowables()));
                }
            }
        });
    }

    private void createSingleErrorDialog(Composite container) {
        // dialog image
        Label imageLabel = new Label(container, 0);
        this.image.setBackground(imageLabel.getBackground());
        imageLabel.setImage(this.image);
        imageLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING));

        // composite to include all text widgets
        Composite textArea = new Composite(container, SWT.NONE);
        GridLayout textAreaLayout = new GridLayout(1, false);
        textAreaLayout.verticalSpacing = FontUtils.getFontHeightInPixels(container.getFont());
        textArea.setLayout(textAreaLayout);
        GridData textAreaLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        textAreaLayoutData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
        textArea.setLayoutData(textAreaLayoutData);

        // message label
        Label messageLabel = new Label(textArea, SWT.WRAP);
        messageLabel.setText(this.message);
        GridData messageLabelGridData = new GridData();
        messageLabelGridData.verticalAlignment = SWT.TOP;
        messageLabelGridData.grabExcessHorizontalSpace = true;
        messageLabel.setLayoutData(messageLabelGridData);

        // details label
        Label detailsLabel = new Label(textArea, SWT.WRAP);
        detailsLabel.setText(this.details);
        GridData detailsLabelGridData = new GridData();
        detailsLabelGridData.verticalAlignment = SWT.TOP;
        detailsLabelGridData.grabExcessHorizontalSpace = true;
        detailsLabel.setLayoutData(detailsLabelGridData);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button copyExceptionButton = createButton(parent, COPY_EXCEPTION_BUTTON_ID, "", false); //$NON-NLS-1$
        copyExceptionButton.setToolTipText(UiMessages.Button_CopyFailuresToClipboard_Tooltip);
        copyExceptionButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
        this.detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL,
                false);
        Button okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        okButton.setFocus();
    }

    @Override
    protected void setButtonLayoutData(Button button) {
        if (button.getData() != null && button.getData().equals(COPY_EXCEPTION_BUTTON_ID)) {
            // do not set a width hint for the copy error button, like it is
            // done in the super implementation
            GridDataFactory.swtDefaults().applyTo(button);
            return;
        }
        super.setButtonLayoutData(button);
    }

    @Override
    protected void initializeBounds() {
        // do not make columns equal width so that we can have a smaller 'copy
        // failure' button
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
            List<Throwable> throwables = getSelectedThrowables();
            copyErrorToClipboard(throwables);
        } else {
            super.buttonPressed(id);
        }
    }

    private List<Throwable> getSelectedThrowables() {
        if (this.errorViewer != null) {
            ISelection selection = this.errorViewer.getSelection();
            if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
                @SuppressWarnings("unchecked")
                List<Throwable> throwableList = ((IStructuredSelection) selection).toList();
                return ImmutableList.copyOf(throwableList);
            } else {
                // if nothing is selected then select the first throwable
                Throwable throwable = (Throwable) this.throwables.get(0);
                this.errorViewer.setSelection(new StructuredSelection(throwable));
                return ImmutableList.of(throwable);
            }
        } else {
            // if the errorViewer is not shown there is only one Throwable shown
            // by this dialog
            Throwable throwable = (Throwable) this.throwables.get(0);
            return ImmutableList.of(throwable);
        }
    }

    protected boolean isMultiErrorDialog() {
        return this.throwables.size() > 1;
    }

    private void copyErrorToClipboard(List<Throwable> throwables) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.message);
        sb.append(LINE_SEPARATOR);
        sb.append(this.details);
        sb.append(LINE_SEPARATOR);
        sb.append(getStackTrace(throwables));

        this.clipboard.setContents(new String[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
    }

    private void toggleStacktraceArea() {
        // show/hide stacktrace
        if (this.stackTraceArea == null) {
            this.stackTraceArea = createStacktraceArea((Composite) getContents(), getSelectedThrowables());
            this.detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
        } else {
            this.stackTraceArea.dispose();
            this.stackTraceArea = null;
            this.detailText = null;
            this.detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
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

    private Control createStacktraceArea(Composite parent, List<Throwable> throwables) {
        // create the stacktrace container area
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout containerLayout = new GridLayout();
        containerLayout.marginHeight = containerLayout.marginWidth = 0;
        container.setLayout(containerLayout);

        // create stacktrace content
        this.detailText = new Text(container, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        this.detailText.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.detailText.setText(getStackTrace(throwables));

        return container;
    }

    private String getStackTrace(List<Throwable> throwables) {
        Writer writer = new StringWriter(1000);
        PrintWriter printWriter = new PrintWriter(writer);
        for (Throwable throwable : throwables) {
            throwable.printStackTrace(printWriter);
            printWriter.write(LINE_SEPARATOR);
        }
        return writer.toString();
    }

    @Override
    public boolean close() {
        if (this.clipboard != null) {
            this.clipboard.dispose();
            this.clipboard = null;
        }
        return super.close();
    }

    public void addException(Throwable... throwable) {
        this.throwables.addAll(ImmutableList.copyOf(throwable));
    }

}
