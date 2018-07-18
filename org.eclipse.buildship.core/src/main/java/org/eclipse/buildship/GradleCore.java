package org.eclipse.buildship;

import org.eclipse.buildship.core.internal.DefaultGradleWorkspace;

public class GradleCore {

	public static GradleWorkspace workspace() {
		return new DefaultGradleWorkspace();
	}
}
