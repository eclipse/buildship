package org.eclipse.buildship;

import org.eclipse.buildship.core.internal.DefaultBuildIdentifierFactory;
import org.eclipse.buildship.core.internal.DefaultGradleWorkspace;

public class GradleCore {
	
	// TODO belongs to GradleWorkspace
	public static BuildIdentifierFactory buildIdentifierFactory() {
		return new DefaultBuildIdentifierFactory();
	}
	
	public static GradleWorkspace workspace() {
		return new DefaultGradleWorkspace();
	}
}
