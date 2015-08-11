package org.eclipse.buildship.core.gradle

import org.gradle.util.GradleVersion
import spock.lang.Ignore
import spock.lang.Specification


@Ignore
// TODO (donat) re-enable this test class once it's clear why it fails on TeamCity
class LimitationsTest extends Specification {

    def "no limitations when using the current version used by Buildship"() {
        given:
        def limitations = new Limitations(GradleVersion.current())

        when:
        def limitationDetails = limitations.limitations

        then:
        limitationDetails.isEmpty()
    }

    def "no limitations when using the base version of the version use by Buildship"() {
        given:
        def limitations = new Limitations(GradleVersion.current().baseVersion)

        when:
        def limitationDetails = limitations.limitations

        then:
        limitationDetails.isEmpty()
    }

    def "single limitation when using final version of 2.4"() {
        given:
        def limitations = new Limitations(GradleVersion.version('2.4'))

        when:
        def limitationDetails = limitations.limitations

        then:
        limitationDetails.size() == 1
    }

    def "single limitation when using a snapshot version of 2.4"() {
        given:
        def limitations = new Limitations(GradleVersion.version('2.4-20150101053008+0000'))

        when:
        def limitationDetails = limitations.limitations

        then:
        limitationDetails.size() == 1
    }

}
