/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.internal;

/**
 * Simplifying abstraction over Eclipse's logging ({@link org.eclipse.core.runtime.ILog}) interface.
 * <p>
 * By using this interface, we can log like this:
 *
 * <pre>
 * try {
 *   ...
 * } catch (Exception e) {
 *   CorePlugin.logger().error(e);
 * }
 * </pre>
 *
 * Instead of doing this:
 *
 * <pre>
 * try {
 *   ...
 * } catch (Exception e) {
 *   CorePlugin.getInstance().getLog().log(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Error occurred", e));
 * }
 * </pre>
 * <p>
 */
public interface Logger {

    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#INFO} severity in Eclipse's log if tracing is enabled.
     *
     * @param message the information to log
     */
    void debug(String message);


    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#INFO} severity in Eclipse's log if tracing is enabled.
     *
     * @param message the information to log
     * @param t the underlying cause
     */
    void debug(String message, Throwable t);

    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#INFO} severity in Eclipse's log.
     *
     * @param message the information to log
     */
    void info(String message);

    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#INFO} severity in Eclipse's log.
     *
     * @param message the information to log
     * @param t the underlying cause
     */
    void info(String message, Throwable t);

    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#WARNING} severity in Eclipse's log.
     *
     * @param message the warning to log
     */
    void warn(String message);

    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#WARNING} severity in Eclipse's log.
     *
     * @param message the warning to log
     * @param t the underlying cause
     */
    void warn(String message, Throwable t);

    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#ERROR} severity in Eclipse's log.
     *
     * @param message the error to log
     */
    void error(String message);

    /**
     * Logs an entry with {@link org.eclipse.core.runtime.IStatus#ERROR} severity in Eclipse's log.
     *
     * @param message the error to log
     * @param t the underlying cause
     */
    void error(String message, Throwable t);

}
