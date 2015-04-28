package org.eclipse.buildship.ui.progressview.model;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.property.list.DelegatingListProperty;
import org.eclipse.core.databinding.property.list.IListProperty;

public class ProgressChildrenListProperty extends DelegatingListProperty {

	@Override
	protected IListProperty doGetDelegate(Object source) {
		if(source instanceof ProgressItem) {
			return BeanProperties.list(ProgressItem.class, "children");
		}
		return null;
	}

}
