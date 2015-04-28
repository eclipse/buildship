package org.eclipse.buildship.ui.progressview.listener;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import org.eclipse.buildship.core.event.BuildLaunchRequestEvent;
import org.eclipse.buildship.ui.progressview.ProgressViewTestListener;
import org.eclipse.buildship.ui.progressview.model.ProgressItem;

public class BuildLaunchRequestListener {

    private ProgressItem root;

    public BuildLaunchRequestListener(ProgressItem root) {
        this.root = root;
    }

    @Subscribe
    public void handleBuildlaunchRequest(BuildLaunchRequestEvent event) {
        List<ProgressItem> rootChildren = Lists.newArrayList();
        ProgressItem buildStarted = new ProgressItem(null, "Gradle Build");
        rootChildren.add(buildStarted);

        List<ProgressItem> buildStartedChildren = Lists.newArrayList();
        final ProgressItem tests = new ProgressItem(null, "Tests");
        buildStartedChildren.add(tests);
        buildStarted.setChildren(buildStartedChildren);

        root.setChildren(rootChildren);

        event.getElement().testProgressListeners(new ProgressViewTestListener(tests));
    }
}
