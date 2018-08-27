package org.eclipse.buildship.stsmigration.internal

import org.eclipse.jface.dialogs.IDialogConstants
import org.eclipse.swt.widgets.Shell
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot
import org.eclipse.swtbot.eclipse.finder.waits.Conditions
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable
import org.eclipse.swtbot.swt.finder.results.BoolResult
import org.eclipse.swtbot.swt.finder.results.VoidResult
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell
import org.eclipse.swtbot.swt.finder.widgets.TimeoutException
import org.eclipse.ui.PlatformUI

import org.eclipse.buildship.stsmigration.internal.StsMigrationDialog
import org.eclipse.buildship.stsmigration.internal.StsMigrationState

import spock.lang.Shared
import spock.lang.Specification

@SuppressWarnings("GroovyAccessibility")
class StsMigrationDialogUiTest extends Specification {

    @Shared
    SWTWorkbenchBot bot = new SWTWorkbenchBot()

    def setupSpec() {
        closeWelcomePageIfAny()
    }

    def setup() {
        closeAllShellsExceptTheApplicationShellAndForceShellActivation()
    }

    def "Can display the dialog"() {
        setup:
        executeInUiThread {
            Shell shell = PlatformUI.workbench.display.activeShell
            StsMigrationDialog.factory().newInstance(shell, Mock(StsMigrationState)).open()
        }
        bot.waitUntil(Conditions.shellIsActive(StsMigrationDialog.DIALOG_TITLE))

        cleanup:
        bot.button(IDialogConstants.OK_LABEL).click()
    }

    def "Can mute-unmute further notifications"() {
        setup:
        StsMigrationState migrationState = Mock(StsMigrationState)
        executeInUiThread {
            Shell shell = PlatformUI.workbench.display.activeShell
            StsMigrationDialog.factory().newInstance(shell, migrationState).open()
        }
        bot.waitUntil(Conditions.shellIsActive(StsMigrationDialog.DIALOG_TITLE))

        expect:
        !bot.checkBox(StsMigrationDialog.MUTE_NOTIFICATION_TEXT).isChecked()

        when:
        bot.checkBox(StsMigrationDialog.MUTE_NOTIFICATION_TEXT).click()

        then:
        bot.checkBox(StsMigrationDialog.MUTE_NOTIFICATION_TEXT).isChecked()
        0 * migrationState.setNotificationMuted(false)
        1 * migrationState.setNotificationMuted(true)

        when:
        bot.checkBox(StsMigrationDialog.MUTE_NOTIFICATION_TEXT).click()

        then:
        !bot.checkBox(StsMigrationDialog.MUTE_NOTIFICATION_TEXT).isChecked()
        1 * migrationState.setNotificationMuted(false)
        0 * migrationState.setNotificationMuted(true)

        cleanup:
        bot.button(IDialogConstants.OK_LABEL).click()
    }

    private def closeWelcomePageIfAny() {
        try {
            SWTBotView view = bot.activeView()
            if (view.title.equals("Welcome")) {
                view.close()
            }
        } catch (WidgetNotFoundException e) {
            e.printStackTrace()
        }
    }

    private def closeAllShellsExceptTheApplicationShellAndForceShellActivation() {
        // in case a UI test fails some shells might not be closed properly
        bot.shells().findAll { it.isOpen() && !isEclipseApplicationShell(it) }.each {
            try {
                it.close()
            } catch (TimeoutException e) {
                e.printStackTrace()
            }
        }

        // http://wiki.eclipse.org/SWTBot/Troubleshooting#No_active_Shell_when_running_SWTBot_tests_in_Xvfb
        UIThreadRunnable.syncExec({ PlatformUI.workbench.activeWorkbenchWindow.shell.forceActive() } as VoidResult)
    }

    private static def isEclipseApplicationShell(SWTBotShell swtBotShell) {
        return UIThreadRunnable.syncExec({ PlatformUI.workbench.activeWorkbenchWindow.shell.equals(swtBotShell.widget) } as BoolResult)
    }

    private static def executeInUiThread(Closure closure) {
        // open dialog in a different thread so that the SWTBot is not blocked
        PlatformUI.workbench.display.asyncExec(closure as Runnable)
    }

}
