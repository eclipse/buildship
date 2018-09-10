package org.eclipse.buildship.core.internal

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import com.google.common.base.Strings

import org.eclipse.buildship.core.*
import org.eclipse.buildship.core.internal.DefaultGradleDistribution.Type

class GradleDistributionInfoTest extends Specification {

    @ClassRule
    @Shared
    TemporaryFolder tempFolder

    def "Validation passes for valid objects"(Type type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(type, configuration)

        expect:
        !distributionInfo.validate().present
        distributionInfo.type == Optional.of(type)

        where:
        type                     | configuration
        Type.WRAPPER             | null
        Type.LOCAL_INSTALLATION  | tempFolder.newFolder().absolutePath
        Type.REMOTE_DISTRIBUTION | 'http://remote.distribution'
        Type.VERSION             | '2.4'
    }

    def "Validation fails for invalid objects"(Type type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(type, configuration)

        expect:
        distributionInfo.validate().present
        distributionInfo.type == type ? Optional.of(type) : Optional.empty()

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

    def "Can serialize and deserialize valid and invalid distributions"(Type type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo1 = new GradleDistributionInfo(type, configuration)
        GradleDistributionInfo distributionInfo2 = GradleDistributionInfo.deserializeFromString(distributionInfo1.serializeToString())

        expect:
        distributionInfo1 == distributionInfo2
        distributionInfo2.type == type ? Optional.of(type) : Optional.empty()
        distributionInfo2.configuration == Strings.nullToEmpty(configuration)

        where:
        type                     | configuration
        Type.WRAPPER             | null
        Type.LOCAL_INSTALLATION  | tempFolder.newFolder().absolutePath
        Type.REMOTE_DISTRIBUTION | 'http://remote.distribution'
        Type.VERSION             | '2.4'
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

    def "Can convert valid distribution info objects to Gradle distributions"(Type type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(type, configuration)

        expect:
        GradleDistribution distribution = distributionInfo.toGradleDistribution()
        distribution.type == type
        distribution.configuration == Strings.nullToEmpty(configuration)

        where:
        type                     | configuration
        Type.WRAPPER             | null
        Type.LOCAL_INSTALLATION  | tempFolder.newFolder().absolutePath
        Type.REMOTE_DISTRIBUTION | 'http://remote.distribution'
        Type.VERSION             | '2.4'
    }

    def "Converting invalid distribution info objects to Gradle distribution throw runtime exception"(Type type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(type, configuration)

        when:
        distributionInfo.toGradleDistribution()

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
