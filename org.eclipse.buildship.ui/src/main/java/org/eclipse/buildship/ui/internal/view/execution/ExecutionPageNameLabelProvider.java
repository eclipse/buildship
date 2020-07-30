/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.view.execution;

import org.gradle.tooling.events.FailureResult;
import org.gradle.tooling.events.FinishEvent;
import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.OperationResult;
import org.gradle.tooling.events.SkippedResult;
import org.gradle.tooling.events.SuccessResult;
import org.gradle.tooling.events.task.TaskFinishEvent;
import org.gradle.tooling.events.task.TaskOperationDescriptor;
import org.gradle.tooling.events.task.TaskSuccessResult;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestOutputDescriptor;

import com.google.common.collect.ImmutableMap;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;

import org.eclipse.buildship.ui.internal.PluginImage;
import org.eclipse.buildship.ui.internal.PluginImages;
import org.eclipse.buildship.ui.internal.util.color.ColorUtils;

/**
 * Label provider for for the first column of {@link ExecutionPage} containing the build operation
 * names.
 */
public final class ExecutionPageNameLabelProvider extends StyledCellLabelProvider implements IStyledLabelProvider {

    private final ImmutableMap<String, ColorDescriptor> customTextToColor;
    private final ResourceManager resourceManager;

    public ExecutionPageNameLabelProvider() {
        this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
        ColorDescriptor decorationsColor = ColorUtils.getDecorationsColorDescriptorFromCurrentTheme();
        this.customTextToColor = ImmutableMap.of("UP-TO-DATE", decorationsColor, "FROM-CACHE", decorationsColor);
    }

    @Override
    public StyledString getStyledText(Object element) {
        if (element instanceof OperationItem) {
            OperationItem operationItem = (OperationItem) element;
            String rawLabel = renderCompact(operationItem);
            StyledString styledLabel = new StyledString(rawLabel);

            // apply custom coloring of those parts of the label for which there is a custom coloring mapping
            for (String text : this.customTextToColor.keySet()) {
                assignColorToText(rawLabel, styledLabel, text);
            }

            return styledLabel;
        } else {
            return null;
        }
    }

    public static String renderVerbose(FinishEvent finishEvent) {
        OperationDescriptor descriptor = finishEvent.getDescriptor();
        return render(null, descriptor, finishEvent, true); // TODO (donat)
    }

    public static String renderCompact(OperationItem operationItem) {
        OperationDescriptor descriptor = operationItem.getDescriptor();
        FinishEvent finishEvent = operationItem.getFinishEvent();
        return render(operationItem, descriptor, finishEvent, false);
    }

    private static String render(OperationItem operationItem, OperationDescriptor descriptor, FinishEvent finishEvent, boolean verbose) {
        if (descriptor instanceof TaskOperationDescriptor) {
            return renderTask(finishEvent, ((TaskOperationDescriptor) descriptor), verbose);
        } else if (descriptor instanceof TestOperationDescriptor) {
            return renderTest(operationItem, descriptor, verbose);
        } else if (descriptor instanceof TestOutputDescriptor) {
            return renderTestOutput((TestOutputDescriptor) descriptor);
        } else {
            return renderOther(descriptor);
        }
    }

    private static String renderTask(FinishEvent finishEvent, TaskOperationDescriptor descriptor, boolean verbose) {
        StringBuilder task = new StringBuilder();

        if (verbose) {
            task.append("Task ");
        }

        task.append(descriptor.getTaskPath());

        if (finishEvent instanceof TaskFinishEvent) {
            if (finishEvent.getResult() instanceof TaskSuccessResult) {
                TaskSuccessResult taskResult = (TaskSuccessResult) finishEvent.getResult();
                if (taskResult.isFromCache()) {
                    task.append(" FROM-CACHE");
                } else if (taskResult.isUpToDate()) {
                    task.append(" UP-TO-DATE");
                }
            }
        }
        return task.toString();
    }

    private static String renderTest(OperationItem operationItem, OperationDescriptor descriptor, boolean verbose) {
        if (verbose) {
            return String.format("Test '%s'", descriptor.getName());
        } else {
            if (operationItem != null && operationItem.getChildren().stream().filter(i -> i.getDescriptor() instanceof TestOutputDescriptor).findFirst().isPresent()) {
                // How to add links to the TODO https://git.eclipse.org/c/platform/eclipse.platform.swt.git/tree/examples/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet356.java
                // IDEA: uses sash to present the output for the selected test
                return descriptor.getName() + " (has output <a href=#>foobar</a>)";
            } else {
                return descriptor.getName();
            }
        }
    }

    private static String renderTestOutput(TestOutputDescriptor descriptor) {
        return String.format("%s: %s", descriptor.getDestination().toString().toLowerCase(), descriptor.getMessage());
    }

    private static String renderOther(OperationDescriptor descriptor) {
        return descriptor.getDisplayName();
    }

    private void assignColorToText(String rawLabel, StyledString styledLabel, final String text) {
        int index = rawLabel.indexOf(text);
        if (index >= 0) {
            Styler styler = new Styler() {

                @Override
                public void applyStyles(TextStyle textStyle) {
                    ColorDescriptor substringColorDescriptor = ExecutionPageNameLabelProvider.this.customTextToColor.get(text);
                    textStyle.foreground = ExecutionPageNameLabelProvider.this.resourceManager.createColor(substringColorDescriptor);

                }
            };
            styledLabel.setStyle(index, text.length(), styler);
        }
    }

    @Override
    public Image getImage(Object element) {
        return element instanceof OperationItem ? calculateImage((OperationItem) element) : null;
    }

    private Image calculateImage(OperationItem operationItem) {
        if (operationItem.getFinishEvent() != null) {
            OperationResult result = operationItem.getFinishEvent().getResult();
            if (result instanceof FailureResult) {
                return PluginImages.OPERATION_FAILURE.withState(PluginImage.ImageState.ENABLED).getImage();
            } else if (result instanceof SkippedResult) {
                return PluginImages.OPERATION_SKIPPED.withState(PluginImage.ImageState.ENABLED).getImage();
            } else if (result instanceof SuccessResult) {
                return PluginImages.OPERATION_SUCCESS.withState(PluginImage.ImageState.ENABLED).getImage();
            } else {
                return null;
            }
        } else {
            return PluginImages.OPERATION_IN_PROGRESS.withState(PluginImage.ImageState.ENABLED).getImage();
        }
    }

    @Override
    public void dispose() {
        this.resourceManager.dispose();
    }
}
