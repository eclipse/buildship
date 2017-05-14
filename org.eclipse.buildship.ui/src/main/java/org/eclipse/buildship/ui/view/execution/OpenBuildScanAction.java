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

package org.eclipse.buildship.ui.view.execution;

import java.net.URL;

import com.google.common.base.Preconditions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.console.ProcessDescription;
import org.eclipse.buildship.core.event.Event;
import org.eclipse.buildship.core.event.EventListener;
import org.eclipse.buildship.core.scan.BuildScanCreatedEvent;

/**
 * Opens build scans from the console output.
 */
public final class OpenBuildScanAction extends Action implements EventListener {

    private final ProcessDescription processDescription;
    private String buildScanUrl;

    public OpenBuildScanAction(ProcessDescription processDescription) {
        this.processDescription = Preconditions.checkNotNull(processDescription);
        setText("Open Scan");
        // TODO (donat) set icon, disabled icon and tooltip
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
