/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import com.google.common.base.Preconditions;

import org.eclipse.buildship.core.omnimodel.OmniAccessRule;

import org.gradle.tooling.model.eclipse.AccessRule;


/**
 * Default implementation of {@link OmniAccessRule}.
 *
 * @author Donat Csikos
 */
final class DefaultOmniAccessRule implements OmniAccessRule {

    private int kind;
    private String pattern;

    DefaultOmniAccessRule(int kind, String pattern) {
        this.kind = Preconditions.checkNotNull(kind);
        this.pattern = Preconditions.checkNotNull(pattern);
    }

    @Override
    public int getKind() {
        return this.kind;
    }

    @Override
    public String getPattern() {
        return this.pattern;
    }

    static DefaultOmniAccessRule from(AccessRule rule) {
        return new DefaultOmniAccessRule(rule.getKind(), rule.getPattern());
    }
}
