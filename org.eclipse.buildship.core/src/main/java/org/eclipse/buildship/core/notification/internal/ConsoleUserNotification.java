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

package org.eclipse.buildship.core.notification.internal;

import org.eclipse.buildship.core.notification.UserNotification;

/**
 * Implementation of the {@link UserNotification} interface that prints all notifications to the standard error.
 */
public final class ConsoleUserNotification implements UserNotification {

    @Override
    public void errorOccurred(String headline, String message, String details, int severity, Throwable... throwable) {
        System.err.println("User notification: headline=[" + headline + "], message=[" + message + "], details=[" + details + "], severity=" + severity + "], exception=[" + throwable + "]");
        throwable.printStackTrace();
    }

}
