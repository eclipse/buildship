package org.eclipse.buildship.core.internal;

import java.io.File;

import org.eclipse.buildship.BuildIdentifier;
import org.eclipse.buildship.BuildIdentifierFactory;

public class DefaultBuildIdentifierFactory implements BuildIdentifierFactory {

	@Override
	public BuildIdentifier from(File projectDir) {
			return new DefaultBuildIdentifier(projectDir);
	}
}
