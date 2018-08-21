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

package org.eclipse.buildship.core.internal.util.variable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;

/**
 * Contains helper methods related to variable expressions.
 */
public final class ExpressionUtils {

    private static final String WORKSPACE_LOC_VARIABLE = "workspace_loc";

    private ExpressionUtils() {
    }

    /**
     * Encodes the workspace location of the given Eclipse project as a variable expression.
     *
     * @param project the project name to encode, must not be null
     * @return the project reference as a variable expression, never null
     */
    public static String encodeWorkspaceLocation(IProject project) {
        return getStringVariableManager().generateVariableExpression(WORKSPACE_LOC_VARIABLE, project.getFullPath().toString());
    }

    /**
     * Decodes the given expression.
     *
     * @param expression the expression to decode, can be null
     * @return the resolved expression, can be null
     */
    public static String decode(String expression) throws CoreException {
        return expression != null ? getStringVariableManager().performStringSubstitution(expression) : null;
    }

    private static IStringVariableManager getStringVariableManager() {
        return VariablesPlugin.getDefault().getStringVariableManager();
    }

}
