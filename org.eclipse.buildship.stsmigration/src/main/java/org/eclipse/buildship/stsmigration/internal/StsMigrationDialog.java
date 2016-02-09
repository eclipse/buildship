/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.stsmigration.internal;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog which informs the user about the migration process from the SpringSource Gradle
 * integration to Buildship.
 */
class StsMigrationDialog extends Dialog {

    private static final String STS_MIGRATION_DOCUMENT_URL = "https://github.com/eclipse/buildship/wiki/Migration-guide-from-STS-to-Buildship";
    private static final String DIALOG_TITLE = "Migration from SpringSource Gradle to Buildship";
    private static final String DIALOG_TEXT = "The development of the STS Gradle plugin is discontinued.\n\nA document "
            + "explaining the migration process from SpringSource Gradle to Buildship can be found <a>here</a>.";
    private static final String MUTE_NOTIFICATION_TEXT = "Don't show this message again";

    private final StsMigrationPlugin plugin;

    private StsMigrationDialog(Shell shell, StsMigrationPlugin plugin) {
        super(shell);
        this.plugin = plugin;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(DIALOG_TITLE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 10).extendedMargins(10, 10, 10, 10).applyTo(container);

        Label dialogIcon = new Label(container, 0);
        dialogIcon.setBackground(container.getBackground());
        dialogIcon.setImage(getShell().getDisplay().getSystemImage(SWT.ICON_INFORMATION));
        dialogIcon.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

        Link migrationLink = new Link(container, SWT.NONE);
        migrationLink.setText(DIALOG_TEXT);
        migrationLink.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true));
        migrationLink.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                // search for the string between the <a> tags in the link's text
                if (event.text.equals("here")) {
                    try {
                        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(STS_MIGRATION_DOCUMENT_URL));
                    } catch (PartInitException e) {
                        StsMigrationDialog.this.plugin.getLog().log(new Status(IStatus.ERROR, StsMigrationPlugin.PLUGIN_ID, "Failed to open external browser", e));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException("Invalid URL", e);
                    }
                }
            }
        });

        Button muteNotificationButton = new Button(container, SWT.CHECK);
        muteNotificationButton.setText(MUTE_NOTIFICATION_TEXT);
        muteNotificationButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 2, 1));
        muteNotificationButton.setSelection(this.plugin.isNotificationMuted());
        muteNotificationButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                StsMigrationDialog.this.plugin.setNotificationMuted(((Button) e.getSource()).getSelection());
            };
        });
        return container;
    }

    /**
     * Factory class for the dialog.
     */
    static class Factory {

        private Factory() {
        }

        public StsMigrationDialog newInstance(Shell shell, StsMigrationPlugin plugin) {
            return new StsMigrationDialog(shell, plugin);
        }
    }

    public static Factory factory() {
        return new Factory();
    }

}
