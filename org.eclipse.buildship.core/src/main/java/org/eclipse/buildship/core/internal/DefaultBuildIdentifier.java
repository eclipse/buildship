package org.eclipse.buildship.core.internal;

import java.io.File;

import org.eclipse.buildship.BuildIdentifier;

public class DefaultBuildIdentifier implements BuildIdentifier {

	private File projectDir;

	public DefaultBuildIdentifier(File projectDir) {
		this.projectDir = projectDir;
	}
	
	public File getProjectDir() {
		return projectDir;
	}
}
