package eclipsebuild.testing

import spock.lang.Specification


class EclipseTestResultProcessorSpec extends Specification {

    def "first test "() {
        given:
        System.out.println("Hello World")

        expect:
        true
    }
}