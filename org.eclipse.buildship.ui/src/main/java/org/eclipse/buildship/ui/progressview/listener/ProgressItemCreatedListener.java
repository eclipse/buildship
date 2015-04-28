package org.eclipse.buildship.ui.progressview.listener;

import com.google.common.eventbus.Subscribe;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import org.eclipse.buildship.ui.progressview.model.internal.ProgressItemCreatedEvent;

public class ProgressItemCreatedListener {

    private TreeViewer viewer;
    private Display display;

    public ProgressItemCreatedListener(TreeViewer viewer) {
        this.viewer = viewer;
        this.display = viewer.getControl().getDisplay();
    }

    @Subscribe
    public void progressItemCreated(ProgressItemCreatedEvent progressItemCreatedEvent) {
        try {
            display.asyncExec(new Runnable() {

                @Override
                public void run() {
                    viewer.expandAll();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
