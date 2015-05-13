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

package org.eclipse.buildship.core.notification;

/**
 * Notifies the user about something that requires his/her attention.
 */
public interface UserNotification {

    /**
     * Notifies the user about the occurrence of an error.
     *
     * @param headline the headline of the error
     * @param message the concise error description
     * @param details the detailed error description
     * @param severity the severity of the error; must be one of the valid {@link org.eclipse.core.runtime.IStatus#getSeverity()} values
     * @param throwable the exception to notify the user about
     */
    void errorOccurred(String headline, String message, String details, int severity, Throwable throwable);

}
