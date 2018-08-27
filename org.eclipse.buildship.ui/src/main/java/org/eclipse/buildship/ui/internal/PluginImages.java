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
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

import org.eclipse.buildship.ui.internal.util.image.ImageUtils;

/**
 * Enumerates all the images used in this plugin. Uses the {@link ImageRegistry} provided by the
 * {@link UiPlugin} for storage and access.
 */
public enum PluginImages implements PluginImage {

    // @formatter:off
    TASK(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/task.png", ImageState.DISABLED, "icons/full/obj16/task_disabled.png")),
    PRIVATE_TASK(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/task_private.png", ImageState.DISABLED, "icons/full/obj16/task_disabled.png")),
    PROJECT_TASK(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/task_project.png", ImageState.DISABLED, "icons/full/obj16/task_disabled.png")),
    PRIVATE_PROJECT_TASK(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/task_project_private.png", ImageState.DISABLED, "icons/full/obj16/task_disabled.png")),
    PROJECT_GROUP(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/project_hierarchy.png")),
    TASK_GROUP(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/task-group.png")),
    SORT_BY_TYPE(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/sort_by_type.png")),
    SORT_BY_VISIBILITY(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/sort_by_visibility.png")),
    RUN_TASKS(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/run_tasks.png", ImageState.DISABLED, "icons/full/obj16/task_disabled.png")),
    REFRESH(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/refresh.png", ImageState.DISABLED, "icons/full/dlcl16/refresh.png")),
    LINK_TO_SELECTION(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/link_to_selection.png")),
    REMOVE_CONSOLE(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/remove_page.png", ImageState.DISABLED, "icons/full/dlcl16/remove_page.png")),
    REMOVE_ALL_CONSOLES(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/remove_all_pages.png", ImageState.DISABLED, "icons/full/dlcl16/remove_all_pages.png")),
    CANCEL_BUILD_EXECUTION(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/cancel_build_execution.png", ImageState.DISABLED, "icons/full/dlcl16/cancel_build_execution.png")),
    RERUN_BUILD(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/rerun.png", ImageState.DISABLED, "icons/full/dlcl16/rerun.png")),
    RERUN_FAILED_TESTS(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/rerun_failed_tests.png", ImageState.DISABLED, "icons/full/dlcl16/rerun_failed_tests.png")),
    EXPAND_ALL(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/expand_all.png")), // unlike collapse_all, this image is not available in the shared platform images
    RUN_CONFIG_TASKS(ImmutableMap.of(ImageState.ENABLED, "icons/full/eview16/run_config_tasks.png")),
    RUN_CONFIG_GRADLE_DISTRIBUTION(ImmutableMap.of(ImageState.ENABLED, "icons/full/eview16/run_config_gradle_distribution.png")),
    RUN_CONFIG_ARGUMENTS(ImmutableMap.of(ImageState.ENABLED, "icons/full/eview16/run_config_arguments.png")),
    SWITCH_TO_CONSOLE(ImmutableMap.of(ImageState.ENABLED, "icons/full/eview16/consoles_view.png")),
    SWITCH_PAGE(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/switch_page.png")),
    REMOVE_PAGE(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/remove_page.png", ImageState.DISABLED, "icons/full/dlcl16/remove_page.png")),
    REMOVE_ALL_PAGES(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/remove_all_pages.png", ImageState.DISABLED, "icons/full/dlcl16/remove_all_pages.png")),
    FILTER_EXECUTION(ImmutableMap.of(ImageState.ENABLED, "icons/full/elcl16/filter.png")),
    OPERATION_IN_PROGRESS(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/operation_inprogress.png")),
    OPERATION_SUCCESS(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/operation_success.png")),
    OPERATION_SKIPPED(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/operation_skipped.png")),
    OPERATION_FAILURE(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/operation_failure.png")),
    PROJECT(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/project.png")),
    JAVA_PROJECT(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/java_project.png")),
    FAULTY_PROJECT(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/faulty_project.png")),
    BUILD_SCAN(ImmutableMap.of(ImageState.ENABLED, "icons/full/obj16/build_scan.png", ImageState.DISABLED, "icons/full/obj16/build_scan_disabled.png"));
    // @formatter:on

    private final ImmutableMap<ImageState, String> images;

    PluginImages(ImmutableMap<ImageState, String> images) {
        this.images = images;
    }

    public void register() {
        for (Map.Entry<ImageState, String> entry : this.images.entrySet()) {
            ImageState state = entry.getKey();
            PluginImageWithState imageWithState = withState(state);
            ImageDescriptor imageDescriptor = ImageUtils.findImageDescriptor(UiPlugin.getInstance().getBundle(), entry.getValue());
            getImageRegistry().put(imageWithState.getKey(), imageDescriptor);
        }
    }

    @Override
    public PluginImageWithState withState(final ImageState state) {
        return new PluginImageWithState() {

            @Override
            public String getKey() {
                return String.format("%s.%s", name(), state);
            }

            @Override
            public Image getImage() {
                Image image = getImageRegistry().get(getKey());
                if (image == null) {
                    throw new IllegalArgumentException(String.format("Image %s in state %s not available in UiPlugin image registry.", name(), state));
                }

                return image;
            }

            @Override
            public ImageDescriptor getImageDescriptor() {
                ImageDescriptor image = getImageRegistry().getDescriptor(getKey());
                if (image == null) {
                    throw new IllegalArgumentException(String.format("Image descriptor %s in state %s not available in UiPlugin image registry.", name(), state));
                }

                return image;
            }

            @Override
            public Image getOverlayImage(List<PluginImageWithState> overlayImages) {
                ImmutableList<String> imageKeys = getImageKeys(overlayImages);
                return ImageUtils.getOverlayImage(getKey(), imageKeys, getImageRegistry());
            }

            @Override
            public ImageDescriptor getOverlayImageDescriptor(List<PluginImageWithState> overlayImages) {
                ImmutableList<String> imageKeys = getImageKeys(overlayImages);
                return ImageUtils.getOverlayImageDescriptor(getKey(), imageKeys, getImageRegistry());
            }

            private ImmutableList<String> getImageKeys(List<PluginImageWithState> overlayImages) {
                return FluentIterable.from(overlayImages).transform(new Function<PluginImageWithState, String>() {

                    @Override
                    public String apply(PluginImageWithState imageWithState) {
                        return imageWithState.getKey();
                    }
                }).toList();
            }

        };
    }

    private ImageRegistry getImageRegistry() {
        return UiPlugin.getInstance().getImageRegistry();
    }

}
