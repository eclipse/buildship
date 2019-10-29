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

import org.eclipse.ui.IStartup;

/**
 * Ensures that the migration dialog is presented to the user upon startup.
 * <p/>
 * Class registered with the {@code org.eclipse.ui.startup} extension point.
 */
public final class Startup implements IStartup {

    @Override
    public void earlyStartup() {
        new StsMigrationService(StsMigrationPlugin.getStsMigrationState(), StsMigrationDialog.factory()).run();
    }

}
