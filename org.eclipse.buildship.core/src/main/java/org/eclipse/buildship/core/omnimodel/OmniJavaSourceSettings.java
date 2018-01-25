/*
 * Copyright (c) 2018 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.omnimodel;

/**
 * Describes the Java source settings for a project.
 *
 * @author Donát Csikós
 */
public interface OmniJavaSourceSettings {

    /**
     * Returns the Java source language level.
     *
     * @return the Java source language level
     */
    OmniJavaVersion getSourceLanguageLevel();

    /**
     * Returns the Java target language level.
     *
     * @return the Java target language level
     */
    OmniJavaVersion getTargetBytecodeLevel();

    /**
     * Returns the description of the Java Runtime.
     *
     * @return the description of the Java Runtime
     */
    OmniJavaRuntime getTargetRuntime();

}
