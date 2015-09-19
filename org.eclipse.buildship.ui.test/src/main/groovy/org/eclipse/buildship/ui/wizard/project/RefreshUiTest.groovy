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

import com.gradleware.tooling.toolingclient.GradleDistribution
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectImportJob
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.core.resources.IProject
import org.eclipse.swt.SWT
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem
import org.junit.Rule
import org.junit.rules.TemporaryFolder

class RefreshUiTest extends SwtBotSpecification {

    @Rule
    TemporaryFolder tempFolder

    def setupSpec() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US"
    }

    def "default Eclipse behaviour is not hindered"() {
        setup:
        File projectFolder = tempFolder.newFolder('project-name')
        new File(projectFolder, 'build.gradle') << ''
        new File(projectFolder, 'settings.gradle') << ''

        when:
        newProjectImportJob(projectFolder).schedule()
        waitForJobsToFinish()
        new File(projectFolder, 'newFile') << ''

        then:
        IProject project = CorePlugin.workspaceOperations().findProjectByName('project-name').get()
        !project.getFile('newFile').exists()

        when:
        performDefaultEclipseRefresh()
        waitForJobsToFinish()

        then:
        project.getFile('newFile').exists()

        cleanup:
        CorePlugin.workspaceOperations().deleteAllProjects(null)
    }

    private static def newProjectImportJob(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(GradleDistribution.fromBuild())
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        new ProjectImportJob(configuration.toFixedAttributes(), configuration.workingSets.getValue(), AsyncHandler.NO_OP)
    }

    private static def performDefaultEclipseRefresh() {
        SWTBotView packageExplorer = bot.viewByTitle("Package Explorer")
        packageExplorer.show()
        packageExplorer.setFocus()
        SWTBotTree tree = packageExplorer.bot().tree()
        SWTBotTreeItem treeItem = tree.getTreeItem("project-name")
        treeItem.select().pressShortcut(0, SWT.F5, (char) 0)
        delay(500)
    }

}
