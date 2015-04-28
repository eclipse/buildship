package org.eclipse.buildship.core.event.internal;

import org.eclipse.buildship.core.event.BuildLaunchRequestEvent;

import com.gradleware.tooling.toolingclient.BuildLaunchRequest;

public class DefaultBuildLaunchRequestEvent extends DefaultGradleEvent<BuildLaunchRequest> implements BuildLaunchRequestEvent {

	public DefaultBuildLaunchRequestEvent(Object source,
			BuildLaunchRequest element) {
		super(source, element);
	}
}
