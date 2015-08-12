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

import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectImportJob
import org.eclipse.buildship.core.projectimport.ProjectPreviewJob
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler

abstract class ProjectImportSpecification extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        CorePlugin.workspaceOperations().deleteAllProjects(null)
        workspaceLocation.listFiles().findAll{ it.isDirectory() && !it.name.startsWith('.') }.each { it.deleteDir() }
    }

    protected def folder(String... names) {
        createFoldersRecursively(tempFolder.root, names as List<String>)
    }

    protected def workspaceFolder(String... names) {
        createFoldersRecursively(workspaceLocation, names as List<String>)
    }

    private def createFoldersRecursively(File root, List<String> names) {
        if (names.size() == 1) {
            createFolderIfNotExists(root)
            createFolderIfNotExists(new File(root, names[0]))
        } else {
            createFolderIfNotExists(new File(createFoldersRecursively(root, names[0..-2]), names.last()))
        }
    }

    private def createFolderIfNotExists(File folder) {
        folder.mkdirs()
        folder.canonicalFile
    }

    protected def file(String... names) {
       createFile(tempFolder.root, names as List<String>)
    }

    protected def workspaceFile(String... names) {
        createFile(workspaceLocation, names as List<String>)
    }

    private def createFile(File root, List<String> names) {
        File parent = createFoldersRecursively(root, names[0..-2])
        File file = new File(parent as File, names[-1] as String)
        file.createNewFile()
        file
    }

    protected static def executeProjectImportAndWait(File location) {
        def job = newProjectImportJob(location, GradleDistribution.fromBuild())
        job.schedule()
        job.join()
    }

    protected static def executeProjectPreviewAndWait(File location, FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler) {
        def job = newProjectPreviewJob(location, GradleDistribution.fromBuild(), resultHandler)
        job.schedule()
        job.join()
    }

    private static def newProjectImportJob(File location, GradleDistribution distribution) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(distribution)
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        new ProjectImportJob(configuration, AsyncHandler.NO_OP)
    }

    private static def newProjectPreviewJob(File location, GradleDistribution distribution, FutureCallback<Pair<OmniBuildEnvironment, OmniGradleBuildStructure>> resultHandler) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(distribution)
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        new ProjectPreviewJob(configuration, [], AsyncHandler.NO_OP, resultHandler)
    }

    protected static def allProjects() {
        workspace.root.projects
    }

    protected static def findProject(String name) {
        def result  = CorePlugin.workspaceOperations().findProjectByName(name).orNull()
        result?.open(null)
        result
    }

    protected static def getWorkspace() {
        LegacyEclipseSpockTestHelper.workspace
    }

    protected static def getWorkspaceLocation() {
        workspace.root.location.toFile()
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
