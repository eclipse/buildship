/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.workspace

import static org.gradle.api.JavaVersion.VERSION_13

import org.gradle.api.JavaVersion
import spock.lang.IgnoreIf

import org.eclipse.core.runtime.IStatus

import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.SynchronizationResult
import org.eclipse.buildship.core.internal.UnsupportedConfigurationException
import org.eclipse.buildship.core.internal.test.fixtures.ProjectSynchronizationSpecification

class ImportingMultipleBuildsWithClashingNames extends ProjectSynchronizationSpecification {

    def "Duplicate root project names are rejected"() {
        setup:
        def firstProject = dir('first') { file 'settings.gradle', "rootProject.name = 'root'" }
        def secondProject = dir('second') { file 'settings.gradle', "rootProject.name = 'root'" }

        when:
        importAndWait(firstProject)
        tryImportAndWait(secondProject)

        then:
        allProjects().size() == 2
        findProject('root')
        findProject('second')
    }

    // TODO (donat) the test randomly imports subprojects from project 'second'
    // ensure that the project synchronization is ordered
    @IgnoreIf({ JavaVersion.current().isCompatibleWith(VERSION_13) }) // Gradle 5.5 can run on Java 12 and below
    def "Same subproject names in different builds interrupt the project synchronization on gradle < 5.5"() {
        setup:
        def firstProject = dir('first') {
            dir 'sub/subsub'
            file 'settings.gradle', "include 'sub:subsub'"
        }
        def secondProject = dir('second') {
            dir 'sub/subsub'
            file 'settings.gradle', "include 'sub:subsub' "
        }

        when:
        importAndWait(firstProject, GradleDistribution.forVersion("5.4.1"))

        then:
        allProjects().size() == 3
        findProject('first')
        findProject('sub')
        findProject('subsub')

        when:
        SynchronizationResult result = tryImportAndWait(secondProject, GradleDistribution.forVersion("5.4.1"))

        then:
        result.status.severity == IStatus.WARNING
        result.status.exception instanceof UnsupportedConfigurationException
    }

    // TODO (donat) the test randomly imports subprojects from project 'second'
    // ensure that the project synchronization is ordered
    def "Same subproject names in different builds are deduplicated"() {
        setup:
        def firstProject = dir('first') {
            dir 'sub/subsub'
            file 'settings.gradle', "include 'sub:subsub'"
        }
        def secondProject = dir('second') {
            dir 'sub/subsub'
            file 'settings.gradle', "include 'sub:subsub' "
        }

        when:
        importAndWait(firstProject)

        then:
        allProjects().size() == 3
        findProject('first')
        findProject('sub')
        findProject('subsub')

        when:
        SynchronizationResult result = tryImportAndWait(secondProject)

        then:
        result.status.severity == IStatus.OK
        findProject('first')
        findProject('sub')
        findProject('subsub')

        findProject('second')
        findProject('second-sub')
        findProject('sub-subsub')
    }

}
