/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.dialog

import org.eclipse.buildship.ui.i18n.UiMessages
import org.eclipse.buildship.ui.notification.ExceptionDetailsDialog
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.core.runtime.IStatus
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException
import org.eclipse.swtbot.swt.finder.waits.Conditions
import org.eclipse.ui.PlatformUI

class ErrorDialogTest extends SwtBotSpecification {

    def cleanup() {
        // press OK to close the dialog
        bot.button(IDialogConstants.OK_LABEL).click()
    }

    def "Can show a simple message"() {
        setup:
        executeInUiThread {
            new ExceptionDetailsDialog(PlatformUI.workbench.display.activeShell, 'title', 'message', 'details', IStatus.WARNING, new RuntimeException('text exception')).open()
        }
    }

    def "Can show multiple exceptions"() {
        setup:
         executeInUiThread {
            def dialog = new ExceptionDetailsDialog(PlatformUI.workbench.display.activeShell, 'title', 'message', 'details', IStatus.WARNING, new RuntimeException('first'))
            dialog.addException(new RuntimeException('second'))
            dialog.open()
        }
        bot.waitUntil(Conditions.shellIsActive(UiMessages.Dialog_Title_Multiple_Errors))

        when:
        bot.table()

        then:
        notThrown(WidgetNotFoundException)
    }

    def "When a exception is selected then the related stacktrace is shown"() {
        setup:
        executeInUiThread {
            def dialog = new ExceptionDetailsDialog(PlatformUI.workbench.display.activeShell, 'title', 'message', 'details', IStatus.WARNING, new RuntimeException('first'))
            dialog.addException(new RuntimeException('second'))
            dialog.open()
        }
        bot.waitUntil(Conditions.shellIsActive(UiMessages.Dialog_Title_Multiple_Errors))

        when:
        bot.table().getTableItem(1).select()
        bot.button(IDialogConstants.SHOW_DETAILS_LABEL).click();

        then:
        bot.text().text.contains("second")
        !bot.text().text.contains("first")
    }

    def "When no exception is selected then all stacktraces are shown"() {
        setup:
        executeInUiThread {
            def dialog = new ExceptionDetailsDialog(PlatformUI.workbench.display.activeShell, 'title', 'message', 'details', IStatus.WARNING, new RuntimeException('first'))
            dialog.addException(new RuntimeException('second'))
            dialog.open()
        }

        bot.waitUntil(Conditions.shellIsActive(UiMessages.Dialog_Title_Multiple_Errors))

        when:
        bot.button(IDialogConstants.SHOW_DETAILS_LABEL).click()

        then:
        String text = bot.text().getText()
        text.contains('first')
        text.contains('second')
    }

    private def executeInUiThread(Closure closure) {
        // open dialog in a different thread so that the SWTBot is not blocked
        PlatformUI.workbench.display.asyncExec(closure as Runnable)
    }

}
