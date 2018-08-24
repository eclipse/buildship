package org.eclipse.buildship.core

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import com.google.common.base.Strings

import org.eclipse.buildship.core.GradleDistributionInfo
import org.eclipse.buildship.core.GradleDistributionType
import org.eclipse.buildship.core.util.binding.Validator

class GradleDistributionInfoTest extends Specification {

    @ClassRule
    @Shared
    TemporaryFolder tempFolder

    def "Validation passes for valid objects"(GradleDistributionType type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = GradleDistributionInfo.from(type, configuration)

        expect:
        !distributionInfo.validate().present
        distributionInfo.type == type

        where:
        type                                       | configuration
        GradleDistributionType.WRAPPER             | null
        GradleDistributionType.LOCAL_INSTALLATION  | tempFolder.newFolder().absolutePath
        GradleDistributionType.REMOTE_DISTRIBUTION | 'http://remote.distribution'
        GradleDistributionType.VERSION             | '2.4'
    }

    def "Validation fails for invalid objects"(GradleDistributionType type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = GradleDistributionInfo.from(type, configuration)

        expect:
        distributionInfo.validate().present
        distributionInfo.type == type ?: GradleDistributionType.INVALID

        where:
        type                                       | configuration
        null                                       | null
        null                                       | ''
        GradleDistributionType.LOCAL_INSTALLATION  | null
        GradleDistributionType.LOCAL_INSTALLATION  | ''
        GradleDistributionType.LOCAL_INSTALLATION  | '/path/to/nonexisting/folder'
        GradleDistributionType.REMOTE_DISTRIBUTION | null
        GradleDistributionType.REMOTE_DISTRIBUTION | ''
        GradleDistributionType.REMOTE_DISTRIBUTION | '[invalid-url]'
        GradleDistributionType.VERSION             | null
        GradleDistributionType.VERSION             | ''
    }

    def "Can serialize and deserialize valid and invalid distributions"(GradleDistributionType type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo1 = GradleDistributionInfo.from(type, configuration)
        GradleDistributionInfo distributionInfo2 = GradleDistributionInfo.deserializeFromString(distributionInfo1.serializeToString())

        expect:
        distributionInfo1 == distributionInfo2
        distributionInfo2.type == type ?: GradleDistributionType.INVALID
        distributionInfo2.configuration == Strings.nullToEmpty(configuration)

        where:
        type                                       | configuration
        GradleDistributionType.WRAPPER             | null
        GradleDistributionType.LOCAL_INSTALLATION  | tempFolder.newFolder().absolutePath
        GradleDistributionType.REMOTE_DISTRIBUTION | 'http://remote.distribution'
        GradleDistributionType.VERSION             | '2.4'
        null                                       | null
        null                                       | ''
        GradleDistributionType.LOCAL_INSTALLATION  | null
        GradleDistributionType.LOCAL_INSTALLATION  | ''
        GradleDistributionType.LOCAL_INSTALLATION  | '/path/to/nonexisting/folder'
        GradleDistributionType.REMOTE_DISTRIBUTION | null
        GradleDistributionType.REMOTE_DISTRIBUTION | ''
        GradleDistributionType.REMOTE_DISTRIBUTION | '[invalid-url]'
        GradleDistributionType.VERSION             | null
        GradleDistributionType.VERSION             | ''
    }

    def "Can convert valid distribution info objects to Gradle distributions"(GradleDistributionType type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = GradleDistributionInfo.from(type, configuration)

        expect:
        GradleDistribution distribution = distributionInfo.toGradleDistribution()
        distribution.distributionInfo.type == type
        distribution.distributionInfo.configuration == Strings.nullToEmpty(configuration)

        where:
        type                                       | configuration
        GradleDistributionType.WRAPPER             | null
        GradleDistributionType.LOCAL_INSTALLATION  | tempFolder.newFolder().absolutePath
        GradleDistributionType.REMOTE_DISTRIBUTION | 'http://remote.distribution'
        GradleDistributionType.VERSION             | '2.4'
    }

    def "Converting invalid distribution info objects to Gradle distribution throw runtime exception"(GradleDistributionType type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo = GradleDistributionInfo.from(type, configuration)

        when:
        distributionInfo.toGradleDistribution()

        then:
        thrown(RuntimeException)

        where:
        type                                       | configuration
        null                                       | null
        null                                       | ''
        GradleDistributionType.LOCAL_INSTALLATION  | null
        GradleDistributionType.LOCAL_INSTALLATION  | ''
        GradleDistributionType.LOCAL_INSTALLATION  | '/path/to/nonexisting/folder'
        GradleDistributionType.REMOTE_DISTRIBUTION | null
        GradleDistributionType.REMOTE_DISTRIBUTION | ''
        GradleDistributionType.REMOTE_DISTRIBUTION | '[invalid-url]'
        GradleDistributionType.VERSION             | null
        GradleDistributionType.VERSION             | ''
    }
}
