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

package org.eclipse.buildship.ui.internal.util.image;

import java.net.URL;
import java.util.List;

import org.osgi.framework.Bundle;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;

/**
 * Contains helper methods related to images.
 */
public final class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Searches and returns the specified image descriptor from the selected plug-in resources.
     *
     * @param bundle the bundle which contains the image
     * @param path the relative path to the image
     * @return the image descriptor, never null
     * @throws IllegalArgumentException if the path is invalid
     */
    public static ImageDescriptor findImageDescriptor(Bundle bundle, String path) {
        // validate arguments
        Preconditions.checkNotNull(bundle);

        // return the descriptor if the location is valid
        URL url = FileLocator.find(bundle, new Path(path), null);
        Preconditions.checkArgument(url != null, String.format("Invalid image path %s.", path));

        return ImageDescriptor.createFromURL(url);
    }

    /**
     * Creates and returns a composite image from the given base image and the given overlay images.
     * The final image is registered in the image registry. If the final image is already available
     * in the image registry, that image is reused. For all the specified image ids, the
     * corresponding images must already be present in the image registry.
     *
     * @param baseImageId the id of the base image
     * @param overlayImageIds the ids of the images to lay over the base image
     * @return the composite image with the overlays applied, never null
     */
    public static Image getOverlayImage(String baseImageId, List<String> overlayImageIds, ImageRegistry imageRegistry) {
        String finalId = getOrCreateOverlayImage(baseImageId, overlayImageIds, imageRegistry);
        return imageRegistry.get(finalId);
    }

    /**
     * Creates and returns a composite image (descriptor) from the given base image and the given
     * overlay images. The final image is registered in the image registry. If the final image is
     * already available in the image registry, that image is reused. For all the specified image
     * ids, the corresponding images must already be present in the image registry.
     *
     * @param baseImageId the id of the base image
     * @param overlayImageIds the ids of the images to lay over the base image
     * @return the descriptor of the composite image with the overlays applied, never null
     */
    public static ImageDescriptor getOverlayImageDescriptor(String baseImageId, List<String> overlayImageIds, ImageRegistry imageRegistry) {
        String finalId = getOrCreateOverlayImage(baseImageId, overlayImageIds, imageRegistry);
        return imageRegistry.getDescriptor(finalId);
    }

    private static String getOrCreateOverlayImage(String baseImageId, List<String> overlayImageIds, final ImageRegistry imageRegistry) {
        Image baseImage = imageRegistry.get(baseImageId);
        String finalId = Joiner.on(',').join(ImmutableList.builder().add(baseImageId).addAll(overlayImageIds).build());
        ImageDescriptor finalDescriptor = imageRegistry.getDescriptor(finalId);
        if (finalDescriptor == null) {
            ImageDescriptor[] overlayDescriptors = FluentIterable.from(overlayImageIds).transform(new Function<String, ImageDescriptor>() {

                @Override
                public ImageDescriptor apply(String imageId) {
                    return imageRegistry.getDescriptor(imageId);
                }
            }).toArray(ImageDescriptor.class);
            imageRegistry.put(finalId, new DecorationOverlayIcon(baseImage, overlayDescriptors));
        }
        return finalId;
    }

}
