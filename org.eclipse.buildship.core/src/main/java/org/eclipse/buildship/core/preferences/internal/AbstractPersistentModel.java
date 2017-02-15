/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.preferences.internal;

import org.eclipse.buildship.core.preferences.PersistentModel;

/**
 * Common superclass for {@link PersistentModel} implementations.
 *
 * @author Donat Csikos
 */
public abstract class AbstractPersistentModel implements PersistentModel {

    @Override
    public boolean isPresent() {
        return false;
    }
}
