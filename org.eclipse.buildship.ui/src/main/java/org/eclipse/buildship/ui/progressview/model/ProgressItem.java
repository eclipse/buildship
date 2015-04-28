package org.eclipse.buildship.ui.progressview.model;

import java.util.ArrayList;
import java.util.List;

import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.ProgressEvent;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

public class ProgressItem extends AbstractModelObject implements IAdaptable {

    private final OperationDescriptor operationDescriptor;

    private ProgressEvent lastProgressEvent;

	private String label;

	private ImageDescriptor image;

	private String duration;

	private List<ProgressItem> children = new ArrayList<ProgressItem>();

	public ProgressItem(OperationDescriptor operationDescriptor) {
	    this(operationDescriptor, operationDescriptor == null ? null : operationDescriptor.getDisplayName());
	}

	public ProgressItem(OperationDescriptor operationDescriptor, String label) {
		this.operationDescriptor = operationDescriptor;
        this.label = label;
	}

	@Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
	    if(getOperationDescriptor() != null && OperationDescriptor.class.equals(adapter)) {
	        return getOperationDescriptor();
	    } else if (getLastProgressEvent() != null && ProgressEvent.class.equals(adapter)) {
            return getLastProgressEvent();
        }

    	return Platform.getAdapterManager().getAdapter(this, adapter);
    }

    public List<ProgressItem> getChildren() {
		return children;
	}

	public void setChildren(List<ProgressItem> children) {
		firePropertyChange("children", this.children, this.children = children);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		firePropertyChange("label", this.label, this.label = label);
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		firePropertyChange("duration", this.duration, this.duration = duration);
	}

	public ImageDescriptor getImage() {
        return image;
    }

    public void setImage(ImageDescriptor image) {
        firePropertyChange("image", this.image, this.image = image);
    }

    public OperationDescriptor getOperationDescriptor() {
        return operationDescriptor;
    }

    public ProgressEvent getLastProgressEvent() {
        return lastProgressEvent;
    }

    public void setLastProgressEvent(ProgressEvent lastProgressEvent) {
        this.lastProgressEvent = lastProgressEvent;
    }

}
