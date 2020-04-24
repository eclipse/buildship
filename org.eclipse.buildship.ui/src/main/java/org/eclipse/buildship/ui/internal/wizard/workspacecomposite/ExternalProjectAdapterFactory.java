package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.core.runtime.IAdapterFactory;

public class ExternalProjectAdapterFactory implements IAdapterFactory {

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof ExternalGradleProject){
			return new ExternalGradleProjectAdapter((ExternalGradleProject) adaptableObject);
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		// TODO Auto-generated method stub
		return null;
	}

}
