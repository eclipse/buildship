/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.databinding.converter;

import org.eclipse.core.databinding.conversion.Converter;

/**
 * This is used to convert Strings to Booleans and Booleans to Strings.
 *
 */
public class StringBooleanConverter extends Converter {

    public StringBooleanConverter() {
        super(null, null);
    }

    @Override
    public Object convert(Object fromObject) {
        if (fromObject instanceof String) {
            return Boolean.parseBoolean((String) fromObject);
        } else if (fromObject instanceof Boolean) {
            return fromObject.toString();
        }
        return null;
    }

}
