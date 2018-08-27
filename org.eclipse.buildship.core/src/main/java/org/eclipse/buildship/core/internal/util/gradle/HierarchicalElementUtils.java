/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.util.gradle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

    public static List<EclipseProject> getAll(EclipseProject model) {
        return getAll(model, EclipseProjectComparator.INSTANCE);
    }

    static <T extends HierarchicalElement> List<T> getAll(T model, Comparator<? super T> comparator) {
        ArrayList<T> all = Lists.newArrayList();
        addRecursively(model, all);
        Collections.sort(all, comparator);
        return ImmutableList.copyOf(all);
    }

    @SuppressWarnings("unchecked")
    private static <T extends HierarchicalElement> void addRecursively(T node, List<T> nodes) {
        nodes.add(node);
        for (HierarchicalElement child : node.getChildren()) {
            addRecursively((T) child, nodes);
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
