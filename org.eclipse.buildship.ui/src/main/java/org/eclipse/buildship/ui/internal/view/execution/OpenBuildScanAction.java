/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.internal.view.execution;

import java.net.URL;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.console.ProcessDescription;
import org.eclipse.buildship.core.internal.event.Event;
import org.eclipse.buildship.core.internal.event.EventListener;
import org.eclipse.buildship.core.internal.scan.BuildScanCreatedEvent;
import org.eclipse.buildship.ui.internal.PluginImage.ImageState;
import org.eclipse.buildship.ui.internal.PluginImages;

/**
 * Opens build scans from the console output.
 */
public final class OpenBuildScanAction extends Action implements EventListener {

    private final ProcessDescription processDescription;
    private String buildScanUrl;

    public OpenBuildScanAction(ProcessDescription processDescription) {
        this.processDescription = Preconditions.checkNotNull(processDescription);
        setToolTipText("Open Build Scan");
        setImageDescriptor(PluginImages.BUILD_SCAN.withState(ImageState.ENABLED).getImageDescriptor());
        setDisabledImageDescriptor(PluginImages.BUILD_SCAN.withState(ImageState.DISABLED).getImageDescriptor());
        setEnabled(false);
        CorePlugin.listenerRegistry().addEventListener(this);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof BuildScanCreatedEvent) {
            BuildScanCreatedEvent buildScanEvent = (BuildScanCreatedEvent) event;
            if (buildScanEvent.getProcessDescription().equals(this.processDescription)) {
                this.buildScanUrl = buildScanEvent.getBuildScanUrl();
                setEnabled(true);
            }
        }
    }

    @Override
    public void run() {
        try {
            PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(this.buildScanUrl));
        } catch (Exception e) {
            CorePlugin.logger().warn("Cannot open " + this.buildScanUrl + " in external browser", e);
        }
    }

    public void dispose() {
        CorePlugin.listenerRegistry().removeEventListener(this);
    }
}
