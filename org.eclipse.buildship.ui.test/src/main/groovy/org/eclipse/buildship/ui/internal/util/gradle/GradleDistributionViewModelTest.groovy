/*******************************************************************************
 * Copyright (c) 2023 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.util.gradle

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

import com.google.common.base.Strings

import org.eclipse.buildship.core.FixedVersionGradleDistribution
import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.LocalGradleDistribution
import org.eclipse.buildship.core.RemoteGradleDistribution
import org.eclipse.buildship.core.WrapperGradleDistribution
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel
import org.eclipse.buildship.ui.internal.util.gradle.GradleDistributionViewModel.Type

class GradleDistributionViewModelTest extends Specification {

    @Shared
    @TempDir
    File tempFolder

    def "Validation passes for valid objects"(GradleDistributionViewModel.Type type, String configuration) {
        setup:
        GradleDistributionViewModel distributionViewModel = new GradleDistributionViewModel(type, configuration)

        expect:
        !distributionViewModel.validate().present
        distributionViewModel.type == Optional.of(type)

        where:
        type                     | configuration
        Type.WRAPPER             | null
        Type.LOCAL_INSTALLATION  | new File(tempFolder, "dir").absolutePath
        Type.REMOTE_DISTRIBUTION | 'http://remote.distribution'
        Type.VERSION             | '2.4'
    }

    def "Validation fails for invalid objects"() {
        setup:
        GradleDistributionViewModel distributionViewModel = new GradleDistributionViewModel(type, configuration)

        expect:
        distributionViewModel.validate().present
        distributionViewModel.type == expectedType

        where:
        type                     | configuration                 | expectedType
        null                     | null                          | Optional.empty()
        null                     | ''                            | Optional.empty()
        Type.LOCAL_INSTALLATION  | null                          | Optional.of(Type.LOCAL_INSTALLATION)
        Type.LOCAL_INSTALLATION  | ''                            | Optional.of(Type.LOCAL_INSTALLATION)
        Type.LOCAL_INSTALLATION  | '/path/to/nonexisting/folder' | Optional.of(Type.LOCAL_INSTALLATION)
        Type.REMOTE_DISTRIBUTION | null                          | Optional.of(Type.REMOTE_DISTRIBUTION)
        Type.REMOTE_DISTRIBUTION | ''                            | Optional.of(Type.REMOTE_DISTRIBUTION)
        Type.REMOTE_DISTRIBUTION | '[invalid-url]'               | Optional.of(Type.REMOTE_DISTRIBUTION)
        Type.VERSION             | null                          | Optional.of(Type.VERSION)
        Type.VERSION             | ''                            | Optional.of(Type.VERSION)
    }

    def "Can convert valid wrapper distribution info objects to Gradle distribution"() {
        setup:
        GradleDistributionViewModel distributionViewModel = new GradleDistributionViewModel(Type.WRAPPER , null)

        expect:
        GradleDistribution distribution = distributionViewModel.toGradleDistribution()
        distribution instanceof WrapperGradleDistribution
    }

    def "Can convert valid local distribution info objects to Gradle distribution"() {
        setup:
        String location = tempFolder.newFolder().absolutePath
        GradleDistributionViewModel distributionViewModel = new GradleDistributionViewModel(Type.LOCAL_INSTALLATION , location)

        expect:
        GradleDistribution distribution = distributionViewModel.toGradleDistribution()
        distribution instanceof LocalGradleDistribution
        distribution.location.absolutePath == location
    }

    def "Can convert valid remote distribution info objects to Gradle distribution"() {
        setup:
        String url = 'http://remote.distribution'
        GradleDistributionViewModel distributionViewModel = new GradleDistributionViewModel(Type.REMOTE_DISTRIBUTION , url)

        expect:
        GradleDistribution distribution = distributionViewModel.toGradleDistribution()
        distribution instanceof RemoteGradleDistribution
        distribution.url.toString() == url
    }

    def "Can convert valid fixed version distribution info objects to Gradle distribution"() {
        setup:
        String version = '4.10'
        GradleDistributionViewModel distributionViewModel = new GradleDistributionViewModel(Type.VERSION , version)

        expect:
        GradleDistribution distribution = distributionViewModel.toGradleDistribution()
        distribution instanceof FixedVersionGradleDistribution
        distribution.version == version
    }

    def "Converting invalid distribution info objects to Gradle distribution throw runtime exception"(GradleDistributionViewModel.Type type, String configuration) {
        setup:
        GradleDistributionViewModel distributionViewModel = new GradleDistributionViewModel(type, configuration)

        when:
        distributionViewModel.toGradleDistribution()

        then:
        thrown(RuntimeException)

        where:
        type                     | configuration
        null                     | null
        null                     | ''
        Type.LOCAL_INSTALLATION  | null
        Type.LOCAL_INSTALLATION  | ''
        Type.LOCAL_INSTALLATION  | '/path/to/nonexisting/folder'
        Type.REMOTE_DISTRIBUTION | null
        Type.REMOTE_DISTRIBUTION | ''
        Type.REMOTE_DISTRIBUTION | '[invalid-url]'
        Type.VERSION             | null
        Type.VERSION             | ''
    }
}
