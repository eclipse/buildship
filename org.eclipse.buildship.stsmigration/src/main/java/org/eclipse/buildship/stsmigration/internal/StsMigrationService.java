/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
