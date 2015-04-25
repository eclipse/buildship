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
package org.eclipse.buildship.ui.depsview;

import org.eclipse.buildship.ui.domain.DependencyNode;
import org.eclipse.buildship.ui.domain.ProjectNode;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.themes.ITheme;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import com.gradleware.tooling.toolingmodel.OmniExternalDependency;
import com.gradleware.tooling.toolingmodel.OmniGradleModuleVersion;
import com.gradleware.tooling.toolingmodel.util.Maybe;

/**
 * Label provider for the {@link DependenciesView}.
 */
public final class DependenciesViewLabelProvider implements ITableLabelProvider, ITableColorProvider {

    private static final int NAME_OR_GROUPID_COLUMN = 0;
    private static final int DESCRIPTION_OR_ARTIFACTID_COLUMN = 1;
    private static final int VERSION_COLUMN = 2;

    private final Color descriptionColor;
    private final WorkbenchLabelProvider workbenchLabelProvider;

    public DependenciesViewLabelProvider() {
        ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
        this.descriptionColor = Preconditions.checkNotNull(theme.getColorRegistry().get("DECORATIONS_COLOR"));
        this.workbenchLabelProvider = new WorkbenchLabelProvider();
    }

    @Override
    public String getColumnText(Object element, int columnIndex) {
        if (element instanceof ProjectNode) {
            return getProjectText((ProjectNode) element, columnIndex);
        } else if (element instanceof DependencyNode) {
            return getDependencyText((DependencyNode) element, columnIndex);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.", element));
        }
    }

    private String getProjectText(ProjectNode project, int columnIndex) {
        switch (columnIndex) {
            case NAME_OR_GROUPID_COLUMN:
                return project.getEclipseProject().getName();
            case DESCRIPTION_OR_ARTIFACTID_COLUMN:
                return project.getEclipseProject().getDescription();
            case VERSION_COLUMN:
                return "";
            default:
                throw new IllegalStateException(String.format("Unknown column index %d.", columnIndex));
        }
    }

    private String getDependencyText(DependencyNode dependencyNode, int columnIndex) {
        OmniExternalDependency dependency = dependencyNode.getDependency();
        Maybe<OmniGradleModuleVersion> versionMaybe = dependency.getGradleModuleVersion();
        OmniGradleModuleVersion version = versionMaybe.isPresent() ? versionMaybe.get() : null;
        switch (columnIndex) {
            case NAME_OR_GROUPID_COLUMN:
                return version != null ? version.getGroup() : dependency.getFile().getName();
            case DESCRIPTION_OR_ARTIFACTID_COLUMN:
                return version != null ? version.getName() : "";
            case VERSION_COLUMN:
                return version != null ? version.getVersion() : "";
            default:
                throw new IllegalStateException(String.format("Unknown column index %d.", columnIndex));
        }
    }

    @Override
    public Image getColumnImage(Object element, int columnIndex) {
        if (element instanceof ProjectNode) {
            return getProjectImage((ProjectNode) element, columnIndex);
        } else if (element instanceof DependencyNode) {
            return getDependencyImage((DependencyNode) element, columnIndex);
        } else {
            throw new IllegalStateException(String.format("Unknown element type of element %s.",
             element));
        }
    }

    private Image getProjectImage(ProjectNode project, int columnIndex) {
        if (columnIndex == NAME_OR_GROUPID_COLUMN) {
            Optional<IProject> workspaceProject = project.getWorkspaceProject();
            return workspaceProject.isPresent() ? this.workbenchLabelProvider.getImage(workspaceProject.get()) : null;
        } else {
            return null;
        }
    }

    private Image getDependencyImage(DependencyNode dependencyNode, int columnIndex) {
        if (columnIndex == NAME_OR_GROUPID_COLUMN) {
            return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAR);
        } else {
            return null;
        }
    }

    @Override
    public Color getForeground(Object element, int columnIndex) {
        return (element instanceof ProjectNode && columnIndex == DESCRIPTION_OR_ARTIFACTID_COLUMN) ? this.descriptionColor : null;
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