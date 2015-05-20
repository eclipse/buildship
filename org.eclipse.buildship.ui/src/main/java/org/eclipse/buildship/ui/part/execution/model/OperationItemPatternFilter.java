/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.part.execution.model;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.buildship.ui.viewer.PatternFilter;


/**
 * Filters out {@code OperationItem} instances whose label does not match the given text.
 */
public final class OperationItemPatternFilter extends PatternFilter {

    @Override
    protected boolean isLeafMatch(Viewer viewer, Object element) {
        if (element instanceof OperationItem) {
            String name = ((OperationItem) element).getName();
            return wordMatches(name);
        }

        return super.isLeafMatch(viewer, element);
    }

}
