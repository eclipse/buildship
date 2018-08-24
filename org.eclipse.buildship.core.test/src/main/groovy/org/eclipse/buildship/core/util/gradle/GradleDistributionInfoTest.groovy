package org.eclipse.buildship.core.util.gradle

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

    def "Validation passes only with semantically valid objects"(GradleDistributionType type, String configuration, boolean isValid) {
        setup:
        GradleDistributionInfo distributionInfo = GradleDistributionInfo.from(type, configuration)
        Validator<GradleDistributionInfo> validator = GradleDistributionInfo.validator()

        expect:
        validator.validate(distributionInfo).present != isValid
        distributionInfo.validate().present != isValid

        where:
        type                                       | configuration                       | isValid
        GradleDistributionType.WRAPPER             | null                                | true
        GradleDistributionType.LOCAL_INSTALLATION  | tempFolder.newFolder().absolutePath | true
        GradleDistributionType.REMOTE_DISTRIBUTION | 'http://remote.distribution'        | true
        GradleDistributionType.VERSION             | '2.4'                               | true
        null                                       | null                                | false
        null                                       | ''                                  | false
        GradleDistributionType.LOCAL_INSTALLATION  | null                                | false
        GradleDistributionType.LOCAL_INSTALLATION  | ''                                  | false
        GradleDistributionType.LOCAL_INSTALLATION  | '/path/to/nonexisting/folder'       | false
        GradleDistributionType.REMOTE_DISTRIBUTION | null                                | false
        GradleDistributionType.REMOTE_DISTRIBUTION | ''                                  | false
        GradleDistributionType.REMOTE_DISTRIBUTION | '[invalid-url]'                     | false
        GradleDistributionType.VERSION             | null                                | false
        GradleDistributionType.VERSION             | ''                                  | false
    }

    def "Can serialize and deserialize valid and invalid distributions"(GradleDistributionType type, String configuration) {
        setup:
        GradleDistributionInfo distributionInfo1 = GradleDistributionInfo.from(type, configuration)
        GradleDistributionInfo distributionInfo2 = GradleDistributionInfo.deserializeFromString(distributionInfo1.serializeToString())

        expect:
        distributionInfo1 == distributionInfo2
        distributionInfo2.type == (type != null ? type : GradleDistributionType.INVALID)
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
}
