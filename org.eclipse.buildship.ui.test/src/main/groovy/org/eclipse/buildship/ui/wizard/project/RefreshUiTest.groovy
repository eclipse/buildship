/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ian Stewart-Binks (Red Hat Inc.) - Bug 473862
 */

package org.eclipse.buildship.ui.wizard.project

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes

import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.swt.SWT
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.NewProjectHandler
import org.eclipse.buildship.core.workspace.SynchronizeGradleProjectJob
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification

class RefreshUiTest extends SwtBotSpecification {

    @Rule
    TemporaryFolder tempFolder

    def setupSpec() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US"
    }

    def "default Eclipse behaviour is not hindered"() {
        setup:
        File projectFolder = tempFolder.newFolder('project-name')
        new File(projectFolder, 'settings.gradle') << ''
        newProjectImportJob(projectFolder).schedule()
        waitForGradleJobsToFinish()
        IProject project = CorePlugin.workspaceOperations().findProjectByName('project-name').get()
        new File(projectFolder, 'newFile') << ''

        when:
        performDefaultEclipseRefresh()
        waitForGradleJobsToFinish()

        then:
        bot.waitUntil(FileExistsCondition.create(project.getFile('newFile')), 5000, 500)
    }

    private static def newProjectImportJob(File location) {
        def attributes = new FixedRequestAttributes(location, null, GradleDistribution.fromBuild(), null, [], [])
        new SynchronizeGradleProjectJob(attributes, NewProjectHandler.IMPORT_AND_MERGE, AsyncHandler.NO_OP)
    }

    private static def performDefaultEclipseRefresh() {
        SWTBotView packageExplorer = bot.viewByTitle("Package Explorer")
        packageExplorer.show()
        packageExplorer.setFocus()
        SWTBotTree tree = packageExplorer.bot().tree()
        SWTBotTreeItem treeItem = tree.getTreeItem("project-name")
        treeItem.select().pressShortcut(0, SWT.F5, (char) 0)
    }

    private static final class FileExistsCondition extends DefaultCondition {

        private IFile file

        private FileExistsCondition(IFile file) { this.file = file }

        static def create(IFile file) { new FileExistsCondition(file) }

        @Override
        public boolean test() throws Exception { file.exists() }

        @Override
        public String getFailureMessage() { "File ${file} does not exist" }
    }

}
