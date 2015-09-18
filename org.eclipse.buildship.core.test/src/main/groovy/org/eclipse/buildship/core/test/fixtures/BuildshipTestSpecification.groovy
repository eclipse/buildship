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

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.google.common.util.concurrent.FutureCallback

import com.gradleware.tooling.toolingclient.GradleDistribution
import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.util.Pair

import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectImportJob
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler

/**
 * Base Spock test specification to verify Buildship functionality against the current state of the
 * workspace.
 */
abstract class BuildshipTestSpecification extends Specification {

    @Rule
    TemporaryFolder tempFolderProvider

    private File externalFolder

    // -- setup and cleanup test environment

    def setup() {
        externalFolder = tempFolderProvider.newFolder('external')
    }

    def cleanup() {
        // delete all project from the workspace and the corresponding content on the file system
        CorePlugin.workspaceOperations().deleteAllProjects(null)
        workspaceFolder.listFiles().findAll{ it.isDirectory() && !it.name.startsWith('.') }.each { it.deleteDir() }
    }

    static File getWorkspaceFolder() {
        LegacyEclipseSpockTestHelper.workspace.root.location.toFile()
    }

    // -- helper methods to create and access file structure --

    protected void createFileStructure(Closure closure) {
        FileStructure.create(externalFolder, closure)
    }

    protected void createWorspaceFileStructure(Closure closure) {
        FileStructure.create(workspaceFolder, closure)
    }

    protected File folder(String location) {
        return new File(externalFolder, location)
    }

    protected File workspaceFolder(String location) {
        return new File(workspaceFolder, location)
    }

    // -- helper methods to load models --

    protected GradleModel loadGradleModel(String location) {
        GradleModel.fromProject(folder(location))
    }

    protected GradleModel loadGradleModelFromWorkspace(String location) {
        GradleModel.fromProject(workspaceFolder(location))
    }

}
