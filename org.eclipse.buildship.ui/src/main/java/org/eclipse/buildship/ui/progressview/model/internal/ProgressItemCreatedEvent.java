package org.eclipse.buildship.ui.progressview.model.internal;

import org.eclipse.buildship.core.event.GradleEvent;
import org.eclipse.buildship.ui.progressview.model.ProgressItem;


public class ProgressItemCreatedEvent implements GradleEvent<ProgressItem> {


    private Object source;
    private ProgressItem progressItem;

    public ProgressItemCreatedEvent(Object source, ProgressItem progressItem) {
        this.source = source;
        this.progressItem = progressItem;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public ProgressItem getElement() {
        return progressItem;
    }
}
