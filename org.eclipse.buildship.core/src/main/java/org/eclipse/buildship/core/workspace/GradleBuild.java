/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 */
package org.eclipse.buildship.core.workspace;

//TODO this should eventually also contain the methods to launch tasks etc.
/**
 * A Gradle build.
 *
 * @author Stefan Oehme
 */
public interface GradleBuild {

    /**
     * Returns the {@link ModelProvider} for this build.
     *
     * @return the model provider, never null
     */
    ModelProvider getModelProvider();
}
