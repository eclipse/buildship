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
