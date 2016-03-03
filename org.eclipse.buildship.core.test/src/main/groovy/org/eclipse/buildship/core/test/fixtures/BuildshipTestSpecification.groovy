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

import java.io.File;

import groovy.lang.Closure;
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import com.google.common.io.Files

import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.jobs.Job

import org.eclipse.buildship.core.CorePlugin

/**
 * Base Spock test specification to verify Buildship functionality against the current state of the
 * workspace.
 */
abstract class BuildshipTestSpecification extends Specification {

    @Rule
    TemporaryFolder tempFolderProvider

    private File externalTestDir

    def setup() {
        externalTestDir = tempFolderProvider.newFolder('external')
    }

    def cleanup() {
        deleteAllProjects()
    }

    private void deleteAllProjects() {
        for (IProject project : CorePlugin.workspaceOperations().allProjects) {
            project.delete(true, true, null);
        }
    }

    protected File dir(String location) {
        def dir = getDir(location)
        dir.mkdirs()
        return dir
    }

    protected File dir(String location, @DelegatesTo(value = FileTreeBuilder, strategy = Closure.DELEGATE_FIRST) Closure config) {
        return new FileTreeBuilder(dir(location)).call(config)
    }

    protected File getDir(String location) {
        return new File(dir, location)
    }

    protected File getDir() {
        externalTestDir
    }

    protected File file(String location) {
        def file = new File(dir, location)
        Files.createParentDirs(file)
        return file
    }

    protected File getFile(String location) {
        return new File(dir, location)
    }


    protected File workspaceDir(String location) {
        def dir = getWorkspaceDir(location)
        dir.mkdirs()
        return dir
    }

    protected File workspaceDir(String location, @DelegatesTo(value = FileTreeBuilder, strategy = Closure.DELEGATE_FIRST) Closure config) {
        return new FileTreeBuilder(workspaceDir(location)).call(config)
    }

    protected File getWorkspaceDir(String location) {
        return new File(workspaceDir, location)
    }

    protected File getWorkspaceDir() {
        LegacyEclipseSpockTestHelper.workspace.root.location.toFile()
    }

    protected File workspaceFile(String location) {
        def file = getWorkspaceFile(location)
        Files.createParentDirs(file)
        return file
    }

    protected File getWorkspaceFile(String location) {
        return new File(workspaceDir, location)
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
