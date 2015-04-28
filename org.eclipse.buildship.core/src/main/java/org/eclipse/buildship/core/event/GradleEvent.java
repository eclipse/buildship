package org.eclipse.buildship.core.event;

public interface GradleEvent<T> {

	public Object getSource();
	
	public T getElement();
}
