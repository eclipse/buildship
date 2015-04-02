/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package eclipsebuild

import org.gradle.api.logging.Logger
import org.gradle.api.logging.LogLevel

/**
 * Output stream forwarding the content of the stream to Gradle logging.
 */
class LogOutputStream extends ByteArrayOutputStream {

    private final Logger logger
    private final LogLevel level

    public LogOutputStream(Logger logger, LogLevel level) {
        this.logger = logger
        this.level = level
    }

    @Override
    public void flush() {
        logger.log(level, toString())
        reset()
    }
}
