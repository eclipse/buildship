/*
* Copyright (c) 2015 the original author or authors.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     Ian Stewart-Binks (Red Hat Inc.) - Bug 473862 - F5 key shortcut doesn't refresh project folder contents
*/
package org.eclipse.buildship.ui.wizard.project;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.BaseSWTBotTest;

public class RefreshUiTest extends BaseSWTBotTest {

    private static final String NATURE_ID = CorePlugin.PLUGIN_ID + ".gradleprojectnature";
    private static final String TEST_FILENAME = "newFile";
    private static final String TEST_PROJECTNAME = "newProject";
    private NullProgressMonitor monitor = new NullProgressMonitor();

    @Test
    public void defaultEclipseBehaviourIsNotHindered() throws Exception {
        IProject project = createProject();
        createFileUnderProject(project);
        performDefaultEclipseRefresh();
        assertTrue(project.getFile(TEST_FILENAME).exists());
        project.delete(true, this.monitor);
    }

    private void performDefaultEclipseRefresh() {
        waitForJobsToFinish();

        SWTBotView packageExplorer = bot.viewByTitle("Package Explorer");
        packageExplorer.show();
        SWTBotTree tree = packageExplorer.bot().tree();
        SWTBotTreeItem treeItem = tree.getTreeItem(TEST_PROJECTNAME);
        treeItem.select().pressShortcut(0, SWT.F5, (char) 0);

        waitForJobsToFinish();
    }

    private IProject createProject() throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject project = workspace.getRoot().getProject("newProject");
        IProjectDescription projectDescription = workspace.newProjectDescription(project.getName());

        projectDescription.setNatureIds(new String[] {NATURE_ID});
        project.create(projectDescription, this.monitor);
        project.open(IResource.NONE, this.monitor);

        return project;
    }

    private void createFileUnderProject(IProject project) throws IOException {
        File file = new File(project.getLocation().toFile(), TEST_FILENAME);
        file.createNewFile();
    }

}
