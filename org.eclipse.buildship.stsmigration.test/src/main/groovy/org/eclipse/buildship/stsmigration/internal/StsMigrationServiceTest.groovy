package org.eclipse.buildship.stsmigration.internal

import groovy.swing.factory.DialogFactory
import spock.lang.Specification

class StsMigrationServiceTest extends Specification {

    def "If the STS plugin is not installed then dialog is not presented"() {
        setup:
        StsMigrationPlugin plugin = createPlugin(false, false)
        StsMigrationDialog.Factory dialogFactory = createDialogFactory()
        StsMigrationService service = new StsMigrationService(plugin, dialogFactory)

        when:
        service.run()

        then:
        0 * dialogFactory.newInstance(null,null).open()
    }

    def "If the STS plugin is installed then dialog is presented"() {
        setup:
        StsMigrationPlugin plugin = createPlugin(true, false)
        StsMigrationDialog.Factory dialogFactory = createDialogFactory()
        StsMigrationService service = new StsMigrationService(plugin, dialogFactory)

        when:
        service.run()

        then:
        1 * dialogFactory.newInstance(null,null).open()
    }

    def "If the notification is muted then the dialog is not presented regardless that the STS plugin is installed"() {
        setup:
        StsMigrationPlugin plugin = createPlugin(stsPluginInstalled, true)
        StsMigrationDialog.Factory dialogFactory = createDialogFactory()
        StsMigrationService service = new StsMigrationService(plugin, dialogFactory)

        when:
        service.run()

        then:
        0 * dialogFactory.newInstance(null,null).open()

        where:
        stsPluginInstalled << [false, true]
    }

    private def createPlugin(boolean stsInstalled, boolean notifMuted) {
        StsMigrationPlugin plugin = Mock()
        plugin.stsPluginInstalled >> stsInstalled
        plugin.notificationMuted >> notifMuted
        plugin
    }

    private def createDialogFactory() {
        StsMigrationDialog dialog = Mock()
        StsMigrationDialog.Factory factory = Mock()
        factory.newInstance(_,_) >> dialog
        factory
    }
}
