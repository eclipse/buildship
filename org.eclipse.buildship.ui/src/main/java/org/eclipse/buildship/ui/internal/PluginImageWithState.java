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
