/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Entry point to access Buildship APIs.
 *
 * @author Donat Csikos
 * @since 3.0
 */
public final class GradleCore {
    public static GradleWorkspace getWorkspace() {
        return CorePlugin.internalGradleWorkspace();
    }
}
