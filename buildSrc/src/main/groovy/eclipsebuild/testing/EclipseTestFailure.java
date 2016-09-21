/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package eclipsebuild.testing;

import java.io.PrintStream;
import java.io.PrintWriter;

public final class EclipseTestFailure extends Throwable {

    // TODO (donat) build scans use StacktaceElement to parse a build failed exception

    private static final long serialVersionUID = 1L;
    private final String trace;

    public EclipseTestFailure(String message, String trace) {
        super(message);
        this.trace = trace;
    }

    @Override
    public void printStackTrace() {
        System.err.println(this.trace);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        s.println(this.trace);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        s.println(this.trace);
    }

    @Override
    public String toString() {
        return this.trace;
    }
}
