/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
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
