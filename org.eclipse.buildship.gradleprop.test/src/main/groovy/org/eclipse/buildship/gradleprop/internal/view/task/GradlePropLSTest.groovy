/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.gradleprop.internal.view.task

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView

class GradlePropLSTest extends BaseTaskViewTest {

    def setup() {
        def project = sampleProject()
        importAndWait(project)
        focusTasksView()
        tree.setFocus()
    }

    def "autocompletion is working in gradle.properties file"() {
        setup:
        // Find and open the Package Explorer view
        SWTBotView packageExplorerView = bot.viewByTitle('Package Explorer')
        packageExplorerView.show()

        // Find and open the 'root_dir' in Package Explorer
        SWTBotTreeItem rootNode = packageExplorerView.bot().tree().getTreeItem('root_dir')
        rootNode.doubleClick()

		SWTBotTreeItem gradlePropertiesItem = packageExplorerView.bot().tree().getTreeItem('root_dir').expand().getNode('gradle.properties')
		gradlePropertiesItem.contextMenu("Open With").menu("Generic Text Editor").click()
		
		SWTBotEclipseEditor editor = bot.activeEditor().toTextEditor()
		
		when:
		List<String> allAvailableProposals = editor.getAutoCompleteProposals('org.')
		editor.typeText('\n')
		
		then:
		allAvailableProposals.size() == 23
		
		when:
		editor.typeText('org.gradle.console=')
		List<String> values = editor.getAutoCompleteProposals('')
		editor.typeText('\n')
		
		then:
		def correctValues = ["auto", "plain", "rich", "verbose"]
		values.equals(new ArrayList<String>(correctValues))
		
		when:
		editor.autoCompleteProposal('org.gradle.debug', 'org.gradle.debug.host')

		then:
		// if previous line throws exception then we get fallen test
		true
		
    }

    private File sampleProject() {
        dir('root_dir') {
            file 'gradle.properties', ""
        }
    }
}