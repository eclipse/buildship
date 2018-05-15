package org.eclipse.buildship;

import java.io.File;

public interface BuildIdentifierFactory {
	
	BuildIdentifier from(File projectDir);
	
}
