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

package org.eclipse.buildship.ui.taskview;

import java.util.List;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.themes.ITheme;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.domain.ProjectNode;
import org.eclipse.buildship.ui.domain.ProjectTaskNode;
import org.eclipse.buildship.ui.domain.TaskSelectorNode;
import org.eclipse.buildship.ui.PluginImageWithState;
import org.eclipse.buildship.ui.PluginImages;

/**
 * Label provider for the {@link TaskView}. Provides the labels, the icons for the table tree, and
 * the coloring for the task description.
 */
public final class TaskViewLabelProvider implements ITableLabelProvider, ITableColorProvider {

    private static final int NAME_COLUMN = 0;
    private static final int DESCRIPTION_COLUMN = 1;

    private final Color descriptionColor;
    private final WorkbenchLabelProvider workbenchLabelProvider;

    public TaskViewLabelProvider() {
        ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        this.descriptionColor = Preconditions.checkNotNull(theme.getColorRegistry().get("DECORATIONS_COLOR"));
        this.workbenchLabelProvider = new WorkbenchLabelProvider();
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof ProjectNode) {
            return getProjectText((ProjectNode) element, columnIndex);
        } else if (element instanceof ProjectTaskNode) {
            return getProjectTaskText((ProjectTaskNode) element, columnIndex);
        } else if (element instanceof TaskSelectorNode) {
            return getTaskSelectorText((TaskSelectorNode) element, columnIndex);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }

    private String getProjectText(ProjectNode project, int columnIndex) {
        switch (columnIndex) {
            case NAME_COLUMN:
                return project.getEclipseProject().getName();
            case DESCRIPTION_COLUMN:
                return project.getEclipseProject().getDescription();
            default:
                throw new IllegalStateException(String.format("Unknown column index %d.", columnIndex));
        }
    }

    private String getProjectTaskText(ProjectTaskNode projectTask, int columnIndex) {
        switch (columnIndex) {
            case NAME_COLUMN:
                return projectTask.getProjectTask().getName();
            case DESCRIPTION_COLUMN:
                return projectTask.getProjectTask().getDescription();
            default:
                throw new IllegalStateException(String.format("Unknown column index %d.", columnIndex));
        }
    }

    private String getTaskSelectorText(TaskSelectorNode taskSelector, int columnIndex) {
        switch (columnIndex) {
            case NAME_COLUMN:
                return taskSelector.getTaskSelector().getName();
            case DESCRIPTION_COLUMN:
                return taskSelector.getTaskSelector().getDescription();
            default:
                throw new IllegalStateException(String.format("Unknown column index %d.", columnIndex));
        }
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof ProjectNode) {
            return getProjectImage((ProjectNode) element, columnIndex);
        } else if (element instanceof ProjectTaskNode) {
            return getProjectTaskImage((ProjectTaskNode) element, columnIndex);
        } else if (element instanceof TaskSelectorNode) {
            return getTaskSelectorImage((TaskSelectorNode) element, columnIndex);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }

    private Image getProjectImage(ProjectNode project, int columnIndex) {
        if (columnIndex == NAME_COLUMN) {
            Optional<IProject> workspaceProject = project.getWorkspaceProject();
            return workspaceProject.isPresent() ? this.workbenchLabelProvider.getImage(workspaceProject.get()) : null;
        } else {
            return null;
        }
    }

    private Image getProjectTaskImage(ProjectTaskNode projectTask, int columnIndex) {
        if (columnIndex == NAME_COLUMN) {
            return getOverlayImageForProjectTask(projectTask);
        } else {
            return null;
        }
    }

    private Image getTaskSelectorImage(TaskSelectorNode taskSelector, int columnIndex) {
        if (columnIndex == NAME_COLUMN) {
            return getOverlayImageForTaskSelector(taskSelector);
        } else {
            return null;
        }
    }

    private Image getOverlayImageForProjectTask(ProjectTaskNode projectTask) {
        ImmutableList.Builder<PluginImageWithState> overlayImages = ImmutableList.builder();
        overlayImages.add(PluginImages.OVERLAY_PROJECT_TASK.withState(ImageState.ENABLED));
        if (!projectTask.isPublic()) {
            overlayImages.add(PluginImages.OVERLAY_PRIVATE_TASK.withState(ImageState.ENABLED));
        }
        return getOverlayImage(overlayImages.build());
    }

    private Image getOverlayImageForTaskSelector(TaskSelectorNode taskSelector) {
        ImmutableList.Builder<PluginImageWithState> overlayImages = ImmutableList.builder();
        overlayImages.add(PluginImages.OVERLAY_TASK_SELECTOR.withState(ImageState.ENABLED));
        if (!taskSelector.isPublic()) {
            overlayImages.add(PluginImages.OVERLAY_PRIVATE_TASK.withState(ImageState.ENABLED));
        }
        return getOverlayImage(overlayImages.build());
    }

    private Image getOverlayImage(List<PluginImageWithState> overlayImages) {
        return PluginImages.TASK.withState(ImageState.ENABLED).getOverlayImage(overlayImages);
    }

    @Override
    public Color getForeground(Object element, int columnIndex) {
        return columnIndex == DESCRIPTION_COLUMN ? this.descriptionColor : null;
    }

    @Override
    public Color getBackground(Object element, int columnIndex) {
        return null;
    }

    @Override
    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    @Override
    public void addListener(ILabelProviderListener listener) {
    }

    @Override
    public void removeListener(ILabelProviderListener listener) {
    }

    @Override
    public void dispose() {
        this.workbenchLabelProvider.dispose();
    }

}
