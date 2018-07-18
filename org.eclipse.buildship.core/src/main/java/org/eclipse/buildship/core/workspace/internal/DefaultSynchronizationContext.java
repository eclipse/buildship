package org.eclipse.buildship.core.workspace.internal;

import org.eclipse.buildship.SynchronizationContext;
import org.eclipse.core.resources.IProject;

public final class DefaultSynchronizationContext implements SynchronizationContext {

	private final IProject project;

	public DefaultSynchronizationContext(IProject project) {
		this.project = project;
	}

	@Override
	public IProject getProject() {
		return this.project;
	}
}
