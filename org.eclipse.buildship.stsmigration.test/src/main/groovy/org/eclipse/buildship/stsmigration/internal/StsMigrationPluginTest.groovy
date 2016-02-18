package org.eclipse.buildship.stsmigration.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.osgi.framework.Bundle
import spock.lang.Specification

import org.eclipse.core.runtime.Platform

class StsMigrationPluginTest extends Specification {

    @Rule
    TemporaryFolder testDir = new TemporaryFolder()

    def "Can detect if the STS plugin is not installed"() {
        expect:
        !StsMigrationPlugin.stsMigrationState.isStsPluginInstalled()
    }

    def "Can detect if the STS plugin is installed"() {
        setup:
        Bundle bundle = createAndInstallFakeStsPlugin()

        expect:
        StsMigrationPlugin.stsMigrationState.isStsPluginInstalled()

        cleanup:
        bundle.uninstall()
    }

    @SuppressWarnings("GroovyAccessibility")
    def "Stores muted notification preference in the configuration scope"() {
        setup:
        StsMigrationPlugin.stsMigrationState.notificationMuted = !isNotificationMuted
        def configurationLocation = new File(Platform.configurationLocation.URL.toURI())
        def configurationFile = new File(configurationLocation, ".settings/${StsMigrationPlugin.PLUGIN_ID}.prefs")
        when:
        StsMigrationPlugin.stsMigrationState.notificationMuted = isNotificationMuted

        then:
        configurationFile.text.contains("${StsMigrationPlugin.NOTIFICATION_MUTED_PROPERTY}=${isNotificationMuted}")

        where:
        isNotificationMuted << [false, true]
    }

    private Bundle createAndInstallFakeStsPlugin() {
        testDir.newFolder('META-INF')
        File manifest = testDir.newFile('META-INF/MANIFEST.MF')
        manifest.text = """Manifest-Version: 1.0
Bundle-ManifestVersion: 2
Bundle-Name: Bundle-Name: SpringSource Tool Suite Gradle Integration (Core)
Bundle-SymbolicName: org.springsource.ide.eclipse.gradle.core;singleto
 n:=true
Bundle-Version: 3.7.2.201511260851-RELEASE
"""
        return StsMigrationPlugin.instance.bundle.bundleContext.installBundle(testDir.root.toURI().toString())
    }

}
