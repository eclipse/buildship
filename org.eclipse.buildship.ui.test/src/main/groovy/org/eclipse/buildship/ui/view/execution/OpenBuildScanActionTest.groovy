package org.eclipse.buildship.ui.view.execution

import com.gradleware.tooling.toolingclient.GradleDistribution

import org.eclipse.debug.core.DebugPlugin
import org.eclipse.debug.core.ILaunch
import org.eclipse.debug.core.ILaunchConfigurationType
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
import org.eclipse.debug.core.ILaunchManager
import org.eclipse.ui.IWorkbenchPage
import org.eclipse.ui.PlatformUI
import org.eclipse.ui.console.ConsolePlugin
import org.eclipse.ui.console.IConsole
import org.eclipse.ui.console.IConsoleListener

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes
import org.eclipse.buildship.core.launch.GradleRunConfigurationDelegate
import org.eclipse.buildship.core.launch.RunGradleBuildLaunchRequestJob
import org.eclipse.buildship.ui.console.GradleConsole
import org.eclipse.buildship.ui.test.fixtures.ProjectSynchronizationSpecification
import org.eclipse.buildship.ui.util.workbench.WorkbenchUtils

class OpenBuildScanActionTest extends ProjectSynchronizationSpecification {

    ConsoleListener consoleListener
    ExecutionsView view

    void setup() {
        runOnUiThread { view = WorkbenchUtils.showView(ExecutionsView.ID, null, IWorkbenchPage.VIEW_ACTIVATE) }
        ConsolePlugin.default.consoleManager.addConsoleListener(consoleListener = new ConsoleListener())
    }

    void cleanup() {
        runOnUiThread { view.removeAllPages() }
        ConsolePlugin.default.consoleManager.removeConsoleListener(consoleListener)
    }

    def "Build doesn't publish build scans"() {
        setup:
        File projectDir = dir('project-without-build-scan') {
            file 'build.gradle', 'task foo { doLast { println "foo" } }'
        }

        when:
        synchronizeAndWait(projectDir)
        launchTaskAndWait(projectDir, 'foo')

        then:
        view.pages.size() == 1
        !view.pages[0].openBuildScanAction.enabled
    }

    def "Build publishes build scan"() {
        setup:
        File projectDir = dir('project-with-fake-build-scan') {
            file 'build.gradle', '''
                task publishFakeBuildScan {
                    doLast {
                        println 'Publishing build information...'
                        println 'https://scans.gradle.com/s/example'
                    }
                }
            '''
        }

        when:
        synchronizeAndWait(projectDir)
        launchTaskAndWait(projectDir, 'publishFakeBuildScan')

        then:
        view.pages.size() == 1
        view.pages[0].openBuildScanAction.enabled
        view.pages[0].openBuildScanAction.buildScanUrl == 'https://scans.gradle.com/s/example'
    }

    def "Two build publish build scans"() {
        setup:
        File projectDir = dir('project-with-fake-build-scan') {
            file 'build.gradle', '''
                task publishFakeBuildScanA {
                    doLast {
                        println 'Publishing build information...'
                        println 'https://scans.gradle.com/s/A'
                    }
                }
                task publishFakeBuildScanB {
                    doLast {
                        println 'Publishing build information...'
                        println 'https://scans.gradle.com/s/B'
                    }
                }
            '''
        }

        when:
        synchronizeAndWait(projectDir)
        launchTaskAndWait(projectDir, 'publishFakeBuildScanA')
        launchTaskAndWait(projectDir, 'publishFakeBuildScanB')

        then:
        view.pages.size() == 2
        view.pages[0].openBuildScanAction.enabled
        view.pages[0].openBuildScanAction.buildScanUrl == 'https://scans.gradle.com/s/A'
        view.pages[1].openBuildScanAction.enabled
        view.pages[1].openBuildScanAction.buildScanUrl == 'https://scans.gradle.com/s/B'
    }

    def "Build publishes real build scan"(String gradleVersion, String buildScanVersion, List<String> arguments) {
        setup:
        File projectDir = dir('buildship-test-project-with-build-scan') {
            file 'build.gradle', """
                buildscript {
                    repositories { maven { url 'https://plugins.gradle.org/m2/' } }
                    dependencies { classpath 'com.gradle:build-scan-plugin:$buildScanVersion' }
                }
                apply plugin: 'com.gradle.build-scan'
                buildScan {
                    server = "https://e.grdev.net"
                    licenseAgreementUrl = 'https://gradle.com/terms-of-service'
                    licenseAgree = 'yes'
                }
                task somethingFunky { doLast { println 'somethingFunky' } }
            """
        }

        when:
        importAndWait(projectDir, GradleDistribution.forVersion(gradleVersion))
        launchTaskAndWait(projectDir, 'somethingFunky', arguments)

        then:
        view.pages.size() == 1
        view.pages[0].openBuildScanAction.enabled
        view.pages[0].openBuildScanAction.buildScanUrl.startsWith 'https://'

        where:
        gradleVersion | buildScanVersion | arguments
        '3.5'         | '1.7.1'          | ['--scan']
        '3.3'         | '1.6'            | ['-Dscan']
    }

    private void launchTaskAndWait(File projectDir, String task, List<String> arguments = []) {
        new RunGradleBuildLaunchRequestJob(createLaunch(task, projectDir, arguments)).schedule()
        waitForGradleJobsToFinish()
        waitForPendingConsoleOutput()
    }

    private void waitForPendingConsoleOutput() {
        waitFor { consoleListener.activeConsole.partitioner.pendingPartitions.empty }
    }

    private void waitFor(int timeout = 5000, Closure condition) {
        long start = System.currentTimeMillis()
        while (!condition.call()) {
            long elapsed = System.currentTimeMillis() - start
            if (elapsed > timeout) {
                throw new RuntimeException('timeout')
            }
            Thread.sleep(100)
        }
    }

    private void runOnUiThread(Closure closure) {
        PlatformUI.workbench.display.syncExec closure as Runnable
    }

    private ILaunch createLaunch(String task, File rootDir, List<String> arguments) {
        ILaunchConfigurationWorkingCopy launchConfig = emptyLaunchConfig()
        GradleRunConfigurationAttributes.applyWorkingDirExpression(rootDir.absolutePath, launchConfig)
        GradleRunConfigurationAttributes.applyTasks([task], launchConfig)
        GradleRunConfigurationAttributes.applyArgumentExpressions(arguments, launchConfig)
        ILaunch launch = Mock(ILaunch)
        launch.launchConfiguration >> launchConfig
        launch
    }

    private ILaunchConfigurationWorkingCopy emptyLaunchConfig() {
        ILaunchManager launchManager = DebugPlugin.default.launchManager
        ILaunchConfigurationType type = launchManager.getLaunchConfigurationType(GradleRunConfigurationDelegate.ID)
        type.newInstance(null, "launch-config-name")
    }

    class ConsoleListener implements IConsoleListener {
        GradleConsole activeConsole

        @Override
        public void consolesAdded(IConsole[] consoles) {
            activeConsole = consoles[0]
        }

        @Override
        public void consolesRemoved(IConsole[] consoles) {
        }
    }
}
