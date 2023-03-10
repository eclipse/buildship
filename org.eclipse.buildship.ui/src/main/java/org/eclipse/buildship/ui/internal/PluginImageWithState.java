/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * Describes an image of the plugin in a given state (enabled, disabled, etc.).
 */
public interface PluginImageWithState {

    String getKey();

    Image getImage();

    ImageDescriptor getImageDescriptor();

    Image getOverlayImage(List<PluginImageWithState> overlayImages);

    ImageDescriptor getOverlayImageDescriptor(List<PluginImageWithState> overlayImages);

}
