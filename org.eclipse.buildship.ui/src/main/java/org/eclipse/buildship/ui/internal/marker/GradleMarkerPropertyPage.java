/*******************************************************************************
 * Copyright (c) 2024 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.marker;

import java.net.URL;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.marker.GradleErrorMarker;

/**
 * Property page for Gradle problems.
 *
 * @author Donat Csikos
 */
public class GradleMarkerPropertyPage extends PropertyPage {

    private Clipboard clipboard;

    public GradleMarkerPropertyPage() {
        super();
        noDefaultAndApplyButton();
    }

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayoutFactory.swtDefaults().numColumns(3 ).margins(0, 0).spacing(10, 6).applyTo(composite);
        GridDataFactory.fillDefaults().applyTo(composite);
        this.clipboard = new Clipboard(parent.getDisplay());

        initializeDialogUnits(composite);
        doCreateContents(composite);
        Dialog.applyDialogFont(composite);

        return composite;
    }

    private void doCreateContents(Composite parent) {
        IMarker marker = (IMarker) getElement().getAdapter(IMarker.class);

        // problem name and id
        Label idLabel = new Label(parent, SWT.NONE);
        idLabel.setText("Problem:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(idLabel);
        Label idValue = new Label(parent, SWT.NONE);
        idValue.setText(idDisplayName(marker.getAttribute(GradleErrorMarker.ATTRIBUTE_ID_DISPLAY_NAME, (String) null), marker.getAttribute(GradleErrorMarker.ATTRIBUTE_FQID, (String) null)));
        GridDataFactory.swtDefaults().span(2, 1).align(SWT.LEFT, SWT.TOP).applyTo(idValue);

        // documentation
        String documentationLink = marker.getAttribute(GradleErrorMarker.ATTRIBUTE_DOCUMENTATION_LINK, "");
        Label documentationLabel = new Label(parent, SWT.NONE);
        documentationLabel.setText("Documentation:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(documentationLabel);

        Link documentationAreaText = new Link(parent, SWT.READ_ONLY);
        GridDataFactory.fillDefaults().hint(100, 1).span(2, 1).grab(true, false).applyTo(documentationAreaText);

        documentationAreaText.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null).openURL(new URL(documentationLink));
                } catch (Exception e) {
                    CorePlugin.logger().warn("Cannot open URL + " + documentationLink, e);
                }
            }
        });
        documentationAreaText.setText("<a>" + documentationLink + "</a>");


        // contextual label
        Label label = new Label(parent, SWT.NONE);
        label.setText("Label:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(label);
        Label labelLvalue = new Label(parent, SWT.NONE);
        labelLvalue.setText(marker.getAttribute(GradleErrorMarker.ATTRIBUTE_LABEL, ""));
        GridDataFactory.swtDefaults().span(2, 1).align(SWT.LEFT, SWT.TOP).applyTo(labelLvalue);

        // details
        Label details = new Label(parent, SWT.NONE);
        details.setText("Details:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(details);
        Label detailsValue = new Label(parent, SWT.NONE);
        detailsValue.setText(marker.getAttribute(GradleErrorMarker.ATTRIBUTE_DETAILS, ""));
        GridDataFactory.swtDefaults().span(2, 1).align(SWT.LEFT, SWT.TOP).applyTo(detailsValue);

        // solutions
        String solutions = marker.getAttribute(GradleErrorMarker.ATTRIBUTE_SOLUTIONS, "");
        Label solutionsLabel = new Label(parent, SWT.READ_ONLY);
        solutionsLabel.setText("Solutions:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(solutionsLabel);

        Text solutionsAreaText = new Text(parent, SWT.MULTI | SWT.READ_ONLY);
        GridDataFactory.fillDefaults().span(1, 1).hint(100, 50).grab(true, false).applyTo(solutionsAreaText);
        solutionsAreaText.setText(solutions);
        new Label(parent, SWT.NONE); // span

        // stacktrace
        String stacktrace = marker.getAttribute(GradleErrorMarker.ATTRIBUTE_STACKTRACE, "(no stacktrace provided)");
        Label stackTraceLabel = new Label(parent, SWT.NONE);
        stackTraceLabel.setText("Stacktrace:");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).applyTo(stackTraceLabel);
        final Text stacktraceAreaText = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().span(1, 1).hint(100, 100).grab(true, true).applyTo(stacktraceAreaText);
        stacktraceAreaText.setText(stacktrace);

        Button copyButton = new Button(parent, SWT.CENTER);
        copyButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_COPY));
        copyButton.setToolTipText("Copy stacktrace");
        GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.BOTTOM).applyTo(copyButton);
        copyButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object[] data = {stacktraceAreaText.getText()};
                Transfer[] dataTypes = {TextTransfer.getInstance()};
                GradleMarkerPropertyPage.this.clipboard.setContents(data, dataTypes);
            }
        });
    }

    private static String idDisplayName(String displayName, String fqid) {
        StringBuilder result = new StringBuilder();
        if (displayName != null) {
            result.append(displayName);
        }
        if (fqid != null) {
            result.append(" (id: ");
            result.append(fqid);
            result.append(")");
        }
        return result.toString();
    }

    public static int countLines(String str) {
        return Lists.newArrayList(Splitter.on(System.lineSeparator()).split(str)).size();
    }
}
