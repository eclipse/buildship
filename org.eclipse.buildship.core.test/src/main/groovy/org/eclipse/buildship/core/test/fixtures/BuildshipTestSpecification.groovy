/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.test.fixtures

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.core.runtime.jobs.Job
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Base Spock test specification to verify Buildship functionality against the current state of the
 * workspace.
 */
abstract class BuildshipTestSpecification extends Specification {

    @Rule
    TemporaryFolder tempFolderProvider

    private File externalTestFolder

    // -- setup and cleanup test environment

    def setup() {
        externalTestFolder = tempFolderProvider.newFolder('external')
    }

    def cleanup() {
        // delete all project from the workspace and the corresponding content on the file system
        CorePlugin.workspaceOperations().deleteAllProjects(null)
        workspaceFolder.listFiles().findAll{ it.isDirectory() && !it.name.startsWith('.') }.each { it.deleteDir() }
    }

    // -- methods to retrieve test folders

    protected File getWorkspaceFolder() {
        LegacyEclipseSpockTestHelper.workspace.root.location.toFile()
    }

    protected File getExternalTestFolder() {
        externalTestFolder
    }

    // -- helper methods to create and access files --

    protected File folder(String location) {
        return new File(externalTestFolder, location)
    }

    protected File workspaceFolder(String location) {
        return new File(workspaceFolder, location)
    }

    protected File file(String location) {
        return new File(externalTestFolder, location)
    }

    protected File workspaceFile(String location) {
        return new File(workspaceFolder, location)
    }

    protected static def waitForJobsToFinish() {
        while (!Job.jobManager.isIdle()) {
            delay(100)
        }
    }

    protected static def delay(long waitTimeMillis) {
        Thread.sleep(waitTimeMillis)
    }

}
