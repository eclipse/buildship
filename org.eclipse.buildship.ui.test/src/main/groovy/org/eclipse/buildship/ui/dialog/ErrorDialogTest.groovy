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

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.ui.i18n.UiMessages
import org.eclipse.buildship.ui.notification.ExceptionDetailsDialog
import org.eclipse.buildship.ui.test.fixtures.SwtBotSpecification
import org.eclipse.core.runtime.IStatus
import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException
import org.eclipse.swtbot.swt.finder.waits.Conditions
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable

class ErrorDialogTest extends SwtBotSpecification {

    def "Open ExceptionDetailsDialog with single Throwable without TableViewer for errors"() {
        setup:
        // open dialog in a different thread so that the SWTBot is not blocked
        bot.activeShell().display.asyncExec({
            def userNatification = CorePlugin.userNotification()
            userNatification.errorOccurred("headline", 'message', 'details', IStatus.ERROR, new NullPointerException('Self instanciated Buildship NPE'))
        } as Runnable
        )
        when:
        SWTBotShell shell = bot.shell("headline")
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive("headline"))
        bot.tableWithId(ExceptionDetailsDialog.class.getName() + "TableViewer:errorViewer")

        then:
        WidgetNotFoundException ex = thrown()

        ex.message == "Could not find widget matching: (of type 'Table' and with key/value (org.eclipse.swtbot.widget.key/org.eclipse.buildship.ui.notification.ExceptionDetailsDialogTableViewer:errorViewer))"

        cleanup:
        // press ok to close the Exception dialog
        bot.button(IDialogConstants.OK_LABEL).click()
    }

    def "Open ExceptionDetailsDialog with multiple Throwables with TableViewer for errors"() {
        setup:
        // open dialog in a different thread so that the SWTBot is not blocked
        bot.activeShell().display.asyncExec({
            def userNatification = CorePlugin.userNotification()
            userNatification.errorOccurred(UiMessages.Dialog_Title_Multiple_Error, 'message', 'details', IStatus.ERROR, new NullPointerException('Self instanciated Buildship NPE'), new RuntimeException('Self instanciated Buildship RuntimeException'))
        } as Runnable
        )
        when:
        SWTBotShell shell = bot.shell(UiMessages.Dialog_Title_Multiple_Error)
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive(UiMessages.Dialog_Title_Multiple_Error))
        bot.tableWithId(ExceptionDetailsDialog.class.getName() + "TableViewer:errorViewer")

        then:
        notThrown WidgetNotFoundException

        cleanup:
        // press ok to close the Exception dialog
        bot.button(IDialogConstants.OK_LABEL).click()
    }

    def "Open ExceptionDetailsDialog, select the second error and show it's details"() {
        setup:
        // open dialog in a different thread so that the SWTBot is not blocked
        bot.activeShell().display.asyncExec({
            def userNatification = CorePlugin.userNotification()
            userNatification.errorOccurred(UiMessages.Dialog_Title_Multiple_Error, 'message', 'details', IStatus.ERROR, new NullPointerException('Self instanciated Buildship NPE'), new RuntimeException('Self instanciated Buildship RuntimeException'))
        } as Runnable
        )
        SWTBotShell shell = bot.shell(UiMessages.Dialog_Title_Multiple_Error)
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive(UiMessages.Dialog_Title_Multiple_Error))
        SWTBotTable errorTable = bot.tableWithId(ExceptionDetailsDialog.class.getName() + "TableViewer:errorViewer")
        bot.table().getTableItem(1).select()

        when:
        bot.button(IDialogConstants.SHOW_DETAILS_LABEL).click();

        String text = bot.text().getText();

        then:
        // ensure that only the error of the selected RuntimeException shown in the details text
        text.contains("RuntimeException")

        !text.contains("NPE")

        cleanup:
        // press ok to close the Exception dialog
        bot.button(IDialogConstants.OK_LABEL).click()
    }

    def "Open ExceptionDetailsDialog with multiple errors and open details without selecting an error"() {
        setup:
        // open dialog in a different thread so that the SWTBot is not blocked
        bot.activeShell().display.asyncExec({
            def userNatification = CorePlugin.userNotification()
            userNatification.errorOccurred(UiMessages.Dialog_Title_Multiple_Error, 'message', 'details', IStatus.ERROR, new NullPointerException('Self instanciated Buildship NPE'), new RuntimeException('Self instanciated Buildship RuntimeException'))
        } as Runnable
        )
        SWTBotShell shell = bot.shell(UiMessages.Dialog_Title_Multiple_Error)
        shell.activate()
        bot.waitUntil(Conditions.shellIsActive(UiMessages.Dialog_Title_Multiple_Error))
        SWTBotTable errorTable = bot.tableWithId(ExceptionDetailsDialog.class.getName() + "TableViewer:errorViewer")

        when:
        bot.button(IDialogConstants.SHOW_DETAILS_LABEL).click();

        String text = bot.text().getText();

        String selectionText = "selectionText"
        String firstItemText = "firstItemText"

        bot.activeShell().display.syncExec(
                {
                    selectionText = errorTable.widget.getSelection()[0].getText()
                    firstItemText = errorTable.getTableItem(0).widget.getText()
                } as Runnable
                )

        then:
        // ensure that the first error in the dialog is selected...
        errorTable.selectionCount() == 1
        selectionText == firstItemText

        // ...  and therefore the NPE error is shown in the details text
        text.contains("NPE")

        !text.contains("RuntimeException")


        cleanup:
        // press ok to close the Exception dialog
        bot.button(IDialogConstants.OK_LABEL).click()
    }

}
