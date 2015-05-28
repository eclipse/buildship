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

package org.eclipse.buildship.ui.databinding.converter;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * This converter inverts a boolean value.
 *
 */
public class BooleanInvert extends Converter {

    public BooleanInvert() {
        super(Boolean.class, Boolean.class);
    }

    @Override
    public Object convert(Object fromObject) {
        return !((Boolean) fromObject);
    }

}
