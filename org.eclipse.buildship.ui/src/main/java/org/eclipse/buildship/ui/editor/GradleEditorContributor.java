/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.EditorActionBarContributor;

import org.eclipse.buildship.ui.PluginImage;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.UiPluginConstants;

/**
 * Action contributor for the Gradle Editor
 *
 * @author Christophe Moine
 */
public class GradleEditorContributor extends EditorActionBarContributor {
	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		toolBarManager.add(new Action("Refresh Gradle Project", PluginImages.REFRESH.withState(PluginImage.ImageState.ENABLED).getImageDescriptor()) {
			@Override
			public void runWithEvent(Event event) {
				IHandlerService service=getActionBars().getServiceLocator().getService(IHandlerService.class);
				try {
					service.executeCommand(UiPluginConstants.REFRESHPROJECT_COMMAND_ID, event);
				} catch (Exception e) {
					UiPlugin.logger().error("Failed to invoke command "+UiPluginConstants.REFRESHPROJECT_COMMAND_ID, e);
				}
			}
		});
	}
}
