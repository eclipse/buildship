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

package org.eclipse.buildship.core.util.collections;

import com.google.common.base.Function;

import org.eclipse.core.runtime.IAdapterManager;

/**
 * {@link Function} which is used to get a certain adapter from an input object.
 *
 * @param <T>
 */
public final class AdapterFunction<T> implements Function<Object, T> {

    private Class<T> adapter;
    private IAdapterManager adapterManager;

    public AdapterFunction(Class<T> adapter, IAdapterManager adapterManager) {
        this.adapter = adapter;
        this.adapterManager = adapterManager;
    }

    @SuppressWarnings({ "unchecked", "cast", "RedundantCast" })
    @Override
    public T apply(Object adaptable) {
        return (T) this.adapterManager.getAdapter(adaptable, this.adapter);
    }

}
