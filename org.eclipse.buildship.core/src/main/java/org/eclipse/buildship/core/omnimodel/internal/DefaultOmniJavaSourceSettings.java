/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel.internal;

import org.eclipse.buildship.core.omnimodel.OmniJavaRuntime;
import org.eclipse.buildship.core.omnimodel.OmniJavaSourceSettings;
import org.eclipse.buildship.core.omnimodel.OmniJavaVersion;

/**
 * Default implementation of the {@link OmniJavaSourceSettings} interface.
 *
 * @author Donát Csikós
 */
public final class DefaultOmniJavaSourceSettings implements OmniJavaSourceSettings {

    private final OmniJavaVersion sourceLanguageLevel;
    private final OmniJavaVersion targetBytecodeLevel;
    private final OmniJavaRuntime targetRuntime;

    private DefaultOmniJavaSourceSettings(OmniJavaVersion sourceLanguageLevel, OmniJavaVersion targetBytecodeLevel, OmniJavaRuntime targeRuntime) {
        this.sourceLanguageLevel = sourceLanguageLevel;
        this.targetBytecodeLevel = targetBytecodeLevel;
        this.targetRuntime = targeRuntime;
    }

    @Override
    public OmniJavaVersion getSourceLanguageLevel() {
        return this.sourceLanguageLevel;
    }

    @Override
    public OmniJavaVersion getTargetBytecodeLevel() {
        return this.targetBytecodeLevel;
    }

    @Override
    public OmniJavaRuntime getTargetRuntime() {
        return this.targetRuntime;
    }

    public static DefaultOmniJavaSourceSettings from(OmniJavaVersion sourceLanguageLevel, OmniJavaVersion targetBytecodeLevel, OmniJavaRuntime targeRuntime) {
        return new DefaultOmniJavaSourceSettings(sourceLanguageLevel, targetBytecodeLevel, targeRuntime);
    }

}
