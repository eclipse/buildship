package org.eclipse.buildship.ui.progressview.model.internal;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.property.list.DelegatingListProperty;
import org.eclipse.core.databinding.property.list.IListProperty;

import org.eclipse.buildship.ui.progressview.model.ProgressItem;

public class ProgressChildrenListProperty extends DelegatingListProperty {

	@Override
	protected IListProperty doGetDelegate(Object source) {
		if(source instanceof ProgressItem) {
			return BeanProperties.list(ProgressItem.class, "children");
		}
		return null;
	}

}
