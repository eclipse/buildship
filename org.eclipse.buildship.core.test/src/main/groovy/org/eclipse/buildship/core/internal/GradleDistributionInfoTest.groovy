package org.eclipse.buildship.core.internal

import org.junit.ClassRule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

import com.google.common.base.Strings

import org.eclipse.buildship.core.FixedVersionGradleDistribution
import org.eclipse.buildship.core.GradleDistribution
import org.eclipse.buildship.core.LocalGradleDistribution
import org.eclipse.buildship.core.RemoteGradleDistribution
import org.eclipse.buildship.core.WrapperGradleDistribution
import org.eclipse.buildship.core.internal.GradleDistributionInfo.Type

class GradleDistributionInfoTest extends Specification {

    @ClassRule
    @Shared
    TemporaryFolder tempFolder

    def "Validation passes for valid objects"(GradleDistributionInfo.Type type, String configuration) {
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

    def "Validation fails for invalid objects"(GradleDistributionInfo.Type type, String configuration) {
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

    def "Can serialize and deserialize valid and invalid distributions"(GradleDistributionInfo.Type type, String configuration) {
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

    def "Can convert valid wrapper distribution info objects to Gradle distribution"() {
        setup:
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(Type.WRAPPER , null)

        expect:
        GradleDistribution distribution = distributionInfo.toGradleDistribution()
        distribution instanceof WrapperGradleDistribution
    }

    def "Can convert valid local distribution info objects to Gradle distribution"() {
        setup:
        String location = tempFolder.newFolder().absolutePath
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(Type.LOCAL_INSTALLATION , location)

        expect:
        GradleDistribution distribution = distributionInfo.toGradleDistribution()
        distribution instanceof LocalGradleDistribution
        distribution.location.absolutePath == location
    }

    def "Can convert valid remote distribution info objects to Gradle distribution"() {
        setup:
        String url = 'http://remote.distribution'
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(Type.REMOTE_DISTRIBUTION , url)

        expect:
        GradleDistribution distribution = distributionInfo.toGradleDistribution()
        distribution instanceof RemoteGradleDistribution
        distribution.url.toString() == url
    }

    def "Can convert valid fixed version distribution info objects to Gradle distribution"() {
        setup:
        String version = '4.10'
        GradleDistributionInfo distributionInfo = new GradleDistributionInfo(Type.VERSION , version)

        expect:
        GradleDistribution distribution = distributionInfo.toGradleDistribution()
        distribution instanceof FixedVersionGradleDistribution
        distribution.version == version
    }

    def "Converting invalid distribution info objects to Gradle distribution throw runtime exception"(GradleDistributionInfo.Type type, String configuration) {
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
