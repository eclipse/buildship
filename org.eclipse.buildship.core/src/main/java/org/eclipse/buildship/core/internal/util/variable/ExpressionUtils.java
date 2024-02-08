/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
