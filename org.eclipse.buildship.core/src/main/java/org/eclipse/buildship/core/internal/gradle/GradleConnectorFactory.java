/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.core.internal.gradle;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import org.gradle.tooling.GradleConnector;

public final class GradleConnectorFactory {

    private final Collection<GradleConnector> connectors = new HashSet<>();
    private final ReentrantLock lock = new ReentrantLock();

    public GradleConnector createGradleConnector() {
        GradleConnector connector = GradleConnector.newConnector();
        this.lock.lock();
        try {
            this.connectors.add(connector);
        } finally {
            this.lock.unlock();
        }
        return connector;
    }

    public void close() {
        this.lock.lock();
        try {
            this.connectors.forEach(GradleConnector::disconnect);
        } finally {
            this.lock.unlock();
        }
    }
}
