/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

/**
 * Implemented by components for which there is a help context id.
 */
public interface HelpContextIdProvider {

    /**
     * Returns the help context id.
     *
     * @return the help context id
     */
    String getHelpContextId();

}
