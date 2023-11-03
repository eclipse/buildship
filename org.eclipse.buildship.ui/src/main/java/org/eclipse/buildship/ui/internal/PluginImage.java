/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal;

/**
 * Describes an image of the plugin. The image can be materialized for different
 * {@link PluginImage.ImageState} values.
 */
public interface PluginImage {

    PluginImageWithState withState(ImageState state);

    /**
     * Enumerates the different states for which an image can have a (possibly different)
     * representation.
     */
    enum ImageState {

        ENABLED, DISABLED

    }

}
