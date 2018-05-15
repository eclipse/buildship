package org.eclipse.buildship;

import org.eclipse.core.runtime.IProgressMonitor;

public interface GradleWorkspace {

	void performImport(BuildIdentifier id, IProgressMonitor monitor, Class<? extends GradleProjectConfigurator> configuratorClass);
}
