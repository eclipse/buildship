package org.eclipse.buildship.ui.viewer.labelprovider;

import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Image;


public class ObservableMapCellWithIconLabelProvider extends ObservableMapCellLabelProvider {


    private ResourceManager resourceManager;

    public ObservableMapCellWithIconLabelProvider(IObservableMap... attributeMaps) {
        super(attributeMaps);
        this.resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    @Override
    public void update(ViewerCell cell) {
        super.update(cell);
        Object element = cell.getElement();
        Object value = attributeMaps[1].get(element);
        if(value instanceof ImageDescriptor) {
            Image image = resourceManager.createImage((ImageDescriptor) value);
            cell.setImage(image);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        resourceManager.dispose();
    }
}
