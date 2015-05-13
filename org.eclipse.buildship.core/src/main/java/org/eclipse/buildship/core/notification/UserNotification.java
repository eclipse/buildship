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
     * @param title problem category
     * @param message concise error description
     * @param details the detailed description
     * @param throwable the exception to notify the user about
     */
    void errorOccurred(String title, String message, String details, Throwable throwable);

}
