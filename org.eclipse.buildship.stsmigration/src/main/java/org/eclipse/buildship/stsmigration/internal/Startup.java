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
