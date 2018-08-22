/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

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
