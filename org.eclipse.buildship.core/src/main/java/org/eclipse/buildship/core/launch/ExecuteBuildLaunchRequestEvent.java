/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - refactoring and integration
 */

package org.eclipse.buildship.core.launch;

import org.eclipse.buildship.core.event.Event;
import org.eclipse.core.runtime.jobs.Job;

import com.gradleware.tooling.toolingclient.Request;

/**
 * Event informing that a {@link Request} is about to be executed. The request
 * can still be modified by the recipients of this event.
 */
public interface ExecuteBuildLaunchRequestEvent extends Event {

    Job getBuildJob();

    Request<Void> getRequest();

    GradleRunConfigurationAttributes getRunConfigurationAttributes();

    String getProcessName();

}
