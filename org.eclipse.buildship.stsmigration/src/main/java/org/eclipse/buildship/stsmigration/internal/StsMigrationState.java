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

/**
 * Exposes the current state of the runtime regarding the STS Gradle to Buildship migration.
 */
public interface StsMigrationState {

    /**
     * Stores whether a notification should be presented to the user upon startup if the STS Gradle
     * integration is installed.
     */
    void setNotificationMuted(boolean muted);

    /***
     * Returns whether a notification should be presented to the user upon startup if the STS Gradle
     * integration is installed.
     */
    boolean isNotificationMuted();

    /**
     * Returns {@code true} if the STS Gradle integration is installed in the current Eclipse
     * instance.
     */
    boolean isStsPluginInstalled();

}
