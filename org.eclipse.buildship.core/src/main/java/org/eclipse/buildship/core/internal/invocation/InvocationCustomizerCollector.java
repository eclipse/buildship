/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.invocation;

import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.invocation.InvocationCustomizer;

/**
 * Retrieves and caches extra Gradle arguments contributed via the {@code invocationcustomizer}
 * extension point.
 *
 * @author Donat Csikos
 */
public final class InvocationCustomizerCollector implements InvocationCustomizer {

    private List<InvocationCustomizer> customizers;

    @Override
    public List<String> getExtraArguments() {
        if (this.customizers == null) {
            this.customizers = CorePlugin.extensionManager().loadCustomizers();
        }
        return collectArguments(this.customizers);
    }

    private static List<String> collectArguments(List<InvocationCustomizer> customizers) {
        List<String> result = Lists.newArrayList();
        for (InvocationCustomizer customizer : customizers) {
            result.addAll(customizer.getExtraArguments());
        }
        return result;
    }
}
