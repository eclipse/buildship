package org.eclipse.buildship.ui.progressview.listener;

import java.util.List;
import java.util.Map;

import org.gradle.tooling.events.OperationDescriptor;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestOperationDescriptor;
import org.gradle.tooling.events.test.TestProgressEvent;
import org.gradle.tooling.events.test.TestProgressListener;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.core.runtime.Platform;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.progressview.model.ProgressItem;
import org.eclipse.buildship.ui.progressview.model.ProgressItemConfigurator;
import org.eclipse.buildship.ui.progressview.model.internal.DefaultProgressItemConfigurator;
import org.eclipse.buildship.ui.progressview.model.internal.ProgressItemCreatedEvent;

public class ProgressViewTestListener implements TestProgressListener {

    private Map<OperationDescriptor, ProgressItem> progressItemMap = Maps.newLinkedHashMap();

    private ProgressItem root;

    private DefaultProgressItemConfigurator progressItemConfigurator;

    public ProgressViewTestListener(ProgressItem testRoot) {
        this.root = testRoot;
    }

    @Override
    public void statusChanged(TestProgressEvent event) {
        TestOperationDescriptor descriptor = event.getDescriptor();
        ProgressItem progressItem = progressItemMap.get(descriptor);
        if (null == progressItem) {
            progressItem = new ProgressItem(descriptor);
            progressItemMap.put(descriptor, progressItem);
            CorePlugin.eventBus().post(new ProgressItemCreatedEvent(this, progressItem));
        }
        // set the last progress event, so that this can be obtained from the viewers selection
        progressItem.setLastProgressEvent(event);

        // Configure progressItem according to the given event
        ProgressItemConfigurator progressItemConfigurator = (ProgressItemConfigurator) Platform.getAdapterManager().getAdapter(event, ProgressItemConfigurator.class);
        if (null == progressItemConfigurator) {
            progressItemConfigurator = getDefaultProgressItemConfigurator(event);
        }
        progressItemConfigurator.configure(progressItem);

        // attach to parent, if necessary
        ProgressItem parentProgressItem = getParent(descriptor);
        if (!parentProgressItem.getChildren().contains(progressItem)) {
            List<ProgressItem> children = Lists.newArrayList(parentProgressItem.getChildren());
            children.add(progressItem);
            parentProgressItem.setChildren(children);
        }
    }

    protected ProgressItemConfigurator getDefaultProgressItemConfigurator(ProgressEvent propressEvent) {
        if (null == progressItemConfigurator) {
            progressItemConfigurator = new DefaultProgressItemConfigurator();
        }
        progressItemConfigurator.setPropressEvent(propressEvent);
        return progressItemConfigurator;
    }

    protected ProgressItem getParent(TestOperationDescriptor descriptor) {
        OperationDescriptor parent = descriptor.getParent();
        ProgressItem parentProgressItem = progressItemMap.get(parent);
        if (null == parentProgressItem) {
            parentProgressItem = root;
        }
        return parentProgressItem;
    }
}
