package org.eclipse.buildship.core.util.gradle

import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper.DistributionType
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class GradleDistributionValidatorTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def "Distribution validator passes only with semantically valid objects"() {
        setup:
        // valid distributions
        def wrapperDistribution = GradleDistributionWrapper.from(DistributionType.WRAPPER, null)
        def localDistribution = GradleDistributionWrapper.from(DistributionType.LOCAL_INSTALLATION, tempFolder.newFolder().absolutePath)
        def remoteDistribution = GradleDistributionWrapper.from(DistributionType.REMOTE_DISTRIBUTION, 'http://remote.distribution')
        def versionDistribution = GradleDistributionWrapper.from(DistributionType.VERSION, '2.4')

        // invalid local distribution objects
        def localDistributionNullConfiguration = GradleDistributionWrapper.from(DistributionType.LOCAL_INSTALLATION, null)
        def localDistributionEmptyConfiguration = GradleDistributionWrapper.from(DistributionType.LOCAL_INSTALLATION, '')
        def localDistributionNonExisting = GradleDistributionWrapper.from(DistributionType.LOCAL_INSTALLATION, '/path/to/nonexisting/folder')

        // invalid remote distribution objects
        def remoteDistributionNullConfiguration = GradleDistributionWrapper.from(DistributionType.REMOTE_DISTRIBUTION, null)
        def remoteDistributionEmptyConfiguration = GradleDistributionWrapper.from(DistributionType.REMOTE_DISTRIBUTION, '')
        def remoteDistributionInvalidUrl = GradleDistributionWrapper.from(DistributionType.REMOTE_DISTRIBUTION, '[invalid-url]')

        // invalid version distribution objects
        def versionDistributionNullConfiguration = GradleDistributionWrapper.from(DistributionType.VERSION, null)
        def versionDistributionEmptyConfiguration = GradleDistributionWrapper.from(DistributionType.VERSION, '')

        // target validator
        def validator = GradleDistributionValidator.gradleDistributionValidator()

        expect:
        !validator.validate(wrapperDistribution).present
        !validator.validate(localDistribution).present
        !validator.validate(remoteDistribution).present
        !validator.validate(versionDistribution).present

        validator.validate(localDistributionNullConfiguration).present
        validator.validate(localDistributionEmptyConfiguration).present
        validator.validate(localDistributionNonExisting).present

        validator.validate(remoteDistributionNullConfiguration).present
        validator.validate(remoteDistributionEmptyConfiguration).present
        validator.validate(remoteDistributionInvalidUrl).present

        validator.validate(versionDistributionNullConfiguration).present
        validator.validate(versionDistributionEmptyConfiguration).present
    }

}
