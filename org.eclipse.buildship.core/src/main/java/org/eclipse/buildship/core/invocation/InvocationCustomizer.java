/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.invocation;

import java.util.List;

/**
 * Defines extra attributes to set for each Gradle invocations.
 * <p/>
 * The interface is used in the {@code org.eclipse.buildship.core.invocationcustomizers} extension point.
 *
 * @author Donat Csikos
 * @since 2.0
 */
public interface InvocationCustomizer {

    /**
     * Returns the list of extra arguments for the Gradle invocations.
     *
     * @return the extra arguments
     */
    List<String> getExtraArguments();
}
