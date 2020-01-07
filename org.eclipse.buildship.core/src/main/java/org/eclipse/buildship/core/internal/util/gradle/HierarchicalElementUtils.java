/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import org.gradle.tooling.model.HierarchicalElement;
import org.gradle.tooling.model.eclipse.EclipseProject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Contains helper methods to traverse {@link HierarchicalElement} instances.
 *
 * @author Donat Csikos
 */
public class HierarchicalElementUtils {

    private HierarchicalElementUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends HierarchicalElement> T getRoot(T model) {
        T root = model;
        while (root.getParent() != null) {
            root = (T) root.getParent();
        }
        return root;
    }

    public static List<EclipseProject> getAll(Collection<? extends EclipseProject> models) {
       return getAll(models, false);
    }

    public static List<EclipseProject> getAllWithDuplicates(Collection<? extends EclipseProject> models) {
        return getAll(models, true);
    }

    private static List<EclipseProject> getAll(Collection<? extends EclipseProject> models, boolean keepDuplicates) {
        ArrayList<EclipseProject> all = Lists.newArrayList();
        HashSet<File> projectDirs = new HashSet<>();
        for (EclipseProject model : models) {
            addRecursively(model, all, projectDirs, keepDuplicates);
        }
        Collections.sort(all, EclipseProjectComparator.INSTANCE);
        return ImmutableList.copyOf(all);
    }


    private static void addRecursively(EclipseProject node, List<EclipseProject> nodes, HashSet<File> projectDirs, boolean keepDuplicates) {
        // If a composite build includes the same build multiple times then the retrieved EclipseProject list will have
        // duplicate entries with the same project directory. This is fine for the 'eclipse' Gradle plugin (it simply
        // writes the same .project and .classpath files multiple times) but causes multiple problems in Eclipse:
        // validation failures, duplicated project notes in the Tasks view, etc. To provide a consistent user experience,
        // those duplicates are eliminated here.
        // Related issue: https://github.com/eclipse/buildship/issues/908
        if (!keepDuplicates && projectDirs.contains(node.getProjectDirectory())) {
            return;
        }

        projectDirs.add(node.getProjectDirectory());
        nodes.add(node);
        for (EclipseProject child : node.getChildren()) {
            addRecursively(child, nodes, projectDirs, keepDuplicates);
        }
    }

    /**
     * Compares {@link EclipseProject}s based on their paths.
     */
    static enum EclipseProjectComparator implements Comparator<EclipseProject> {

        INSTANCE;

        @Override
        public int compare(EclipseProject o1, EclipseProject o2) {
            Path p1 = Path.from(o1.getGradleProject().getPath());
            Path p2 = Path.from(o2.getGradleProject().getPath());
            return p1.compareTo(p2);
        }
    }
}
