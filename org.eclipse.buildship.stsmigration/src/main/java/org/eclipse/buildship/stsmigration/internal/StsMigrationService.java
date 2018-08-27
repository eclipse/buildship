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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Shows the {@link StsMigrationDialog} if the SpringSource Gradle integration is installed in the
 * current Eclipse instance and the user hasn't muted the notification.
 */
final class StsMigrationService {

    private final StsMigrationState migrationState;
    private final StsMigrationDialog.Factory dialogFactory;

    StsMigrationService(StsMigrationState migrationState, StsMigrationDialog.Factory dialogFactory) {
        this.migrationState = migrationState;
        this.dialogFactory = dialogFactory;
    }

    void run() {
        if (shouldDisplayNotification()) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

                @Override
                public void run() {
                    Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
                    StsMigrationDialog dialog = StsMigrationService.this.dialogFactory.newInstance(shell, StsMigrationService.this.migrationState);
                    dialog.open();
                }
            });
        }
    }

    private boolean shouldDisplayNotification() {
        return this.migrationState.isStsPluginInstalled() && !this.migrationState.isNotificationMuted();
    }

}
