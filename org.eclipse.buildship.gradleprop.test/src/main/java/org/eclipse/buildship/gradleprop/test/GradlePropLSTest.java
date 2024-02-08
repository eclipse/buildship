/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.test;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.eclipse.core.resources.IProject;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class GradlePropLSTest extends SwtSpecification {

    @Test
    public void autocompletionWorksForGradlePropertiesFile() throws Exception {
        // setup
        IProject project = createProject("projectName");
        project.getFile("gradle.properties").create(new ByteArrayInputStream("".getBytes()), true, null);

        SWTBotView packageExplorerView = bot.viewByTitle("Package Explorer");
        packageExplorerView.show();
        SWTBotTreeItem rootNode = packageExplorerView.bot().tree().getTreeItem("projectName");
        rootNode.doubleClick();
        SWTBotTreeItem gradlePropertiesItem = packageExplorerView.bot().tree().getTreeItem("projectName").expand().getNode("gradle.properties");
        gradlePropertiesItem.contextMenu("Open With").menu("Generic Text Editor").click();
        SWTBotEclipseEditor editor = bot.activeEditor().toTextEditor();

        // smoke test proposal content: proposed values for a specific Gradle property
        editor.typeText("org.gradle.console=");
        List<String> allAvailableProposals = editor.getAutoCompleteProposals("");
        assertEquals(Arrays.asList("auto", "plain", "rich", "verbose"), allAvailableProposals);
    }
}
