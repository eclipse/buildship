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

package org.eclipse.buildship.ui.internal.wizard;

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
