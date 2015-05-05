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

package org.eclipse.buildship.core;

/**
 * Contract how to send notifications to the user.
 * <p/>
 * In essence this specifies what to display on the UI when an event occurs.
 */
public interface UserNotification {

    /**
     * Notifies the user about an exception.
     *
     * @param message a concise phrase what happened
     * @param summary one or two sentence long description of the problem
     * @param exception the exception to notify the user about
     */
    void notifyAboutException(String message, String summary, Exception exception);
}
