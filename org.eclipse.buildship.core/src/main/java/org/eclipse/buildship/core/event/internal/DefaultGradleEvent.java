package org.eclipse.buildship.core.event.internal;

import org.eclipse.buildship.core.event.GradleEvent;

public class DefaultGradleEvent<T> implements GradleEvent<T> {
	
	private Object source;
	private T element;

	public DefaultGradleEvent(Object source, T element) {
		this.source = source;
		this.element = element;
	}

	@Override
	public Object getSource() {
		return source;
	}

	@Override
	public T getElement() {
		return element;
	}

}
