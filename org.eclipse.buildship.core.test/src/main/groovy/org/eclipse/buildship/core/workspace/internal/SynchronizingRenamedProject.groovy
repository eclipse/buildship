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

package org.eclipse.buildship.core.workspace.internal

import spock.lang.Ignore;

import com.google.common.util.concurrent.FutureCallback

import com.gradleware.tooling.toolingmodel.OmniBuildEnvironment
import com.gradleware.tooling.toolingmodel.OmniGradleBuildStructure
import com.gradleware.tooling.toolingmodel.util.Pair

import org.eclipse.buildship.core.test.fixtures.ProjectSynchronizationSpecification;;
import org.eclipse.core.runtime.Path

class SynchronizingRenamedProject extends ProjectSynchronizationSpecification {

    def "Project name is updated when it changes in Gradle"() {
        setup:
        def sample = dir('sample')
        importAndWait(sample)

        expect:
        findProject(sample.name)

        when:
        renameInGradle(sample, "custom-sample")
        synchronizeAndWait(sample)

        then:
        findProject('custom-sample')
    }

    def "Project name is not updated if it conflicts with unrelated workspace project"() {
        setup:
        def sample = dir('sample')
        importAndWait(sample)
        def alreadyThere = newProject("already-there")

        expect:
        findProject(sample.name)

        when:
        renameInGradle(sample, "already-there")
        synchronizeAndWait(sample)

        then:
        findProject('already-there') == alreadyThere
    }

    def "Projects in the same build can be renamed in cycles"() {
        def root = dir('root') {
            dir 'a'
            dir 'b'
            file 'settings.gradle', "include 'a', 'b'"
        }
        importAndWait(root)
        fileTree(root) {
            renameInGradle(dir('a'), "b")
            renameInGradle(dir('b'), "a")
        }

        when:
        synchronizeAndWait(root)

        then:
        findProject("a").location.lastSegment() == "b"
        findProject("b").location.lastSegment() == "a"
    }

    @Ignore("Adjust this once composites can be imported")
    def "Projects can be renamed in cycles across the workspace"() {
        setup:
        def first = dir('first') {
            dir 'a'
            file 'settings.gradle', "include 'a'"
        }
        def second = dir('second') {
            dir 'b'
            file 'settings.gradle', "include 'b'"
        }
        importAndWait(first)
        importAndWait(second)

        fileTree(first) {
            renameInGradle(dir('a'), "b")
        }
        fileTree(second) {
            renameInGradle(dir('b'), "a")
        }
        when:
        synchronizeAndWait()

        then:
        findProject('a').location.lastSegment() == "b"
        findProject('b').location.lastSegment() == "a"
    }

    def "Cyclic renaming also works for new subprojects"() {
        def root = dir('root') {
            dir 'a'
            file 'settings.gradle', "include 'a'"
        }
        importAndWait(root)
        fileTree(root) {
            dir 'b'
            file('settings.gradle').text = "include 'a', 'b'"
            renameInGradle(dir('a'), "b")
            renameInGradle(dir('b'), "a")
        }

        when:
        synchronizeAndWait(root)

        then:
        findProject("a").location.lastSegment() == "b"
        findProject("b").location.lastSegment() == "a"
    }

    private void renameInGradle(File project, String newName) {
        fileTree(project) {
            file 'build.gradle', """
                apply plugin: 'eclipse'
                eclipse.project.name = '${newName}'
            """
        }
    }

}
