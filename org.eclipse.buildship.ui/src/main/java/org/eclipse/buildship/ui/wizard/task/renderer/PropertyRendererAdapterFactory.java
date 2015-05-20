package org.eclipse.buildship.ui.wizard.task.renderer;

import org.eclipse.core.runtime.IAdapterFactory;

import org.eclipse.buildship.core.model.taskmetadata.TaskPropertyTypes;

public class PropertyRendererAdapterFactory implements IAdapterFactory {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getAdapter(Object adaptableObject, Class adapterType) {
        if (TaskPropertyTypes.Boolean.equals(adaptableObject)) {
            return new BooleanPropertyRenderer();
        }
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Class[] getAdapterList() {
        return new Class[] { PropertyRenderer.class };
    }

}
