package org.eclipse.buildship.core.internal.util.binding

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification


class ValidatorsTest extends Specification {

    @Rule
    TemporaryFolder tempFolder

    def "noOp never invalidates a value"() {
        setup:
        def op = Validators.noOp()

        expect:
        !op.validate(null).present
        !op.validate("something").present
        !op.validate(new File("somewhere")).present
    }

    def "Required directory validator passes only existing directory"() {
        given:
        def nullFolder = null
        def existingFile = tempFolder.newFile('existing-file')
        def existingFolder = existingFile.parentFile
        def nonExistingFile = new File('/nonexistingFolder/nonexistingFile')
        def nonExistingFolder = nonExistingFile.parentFile
        def validator = Validators.requiredDirectoryValidator('somePrefix')

        expect:
        validator.validate(nullFolder).present
        validator.validate(existingFile).present
        !validator.validate(existingFolder).present
        validator.validate(nonExistingFile).present
        validator.validate(nonExistingFolder).present
    }

    def "Optional directory validator passes existing directories and null values"() {
        setup:
        def nullFolder = null
        def existingFile = tempFolder.newFile('existing-file')
        def existingFolder = existingFile.parentFile
        def nonExistingFile = new File('/nonexistingFolder/nonexistingFile')
        def nonExistingFolder = nonExistingFile.parentFile
        def validator = Validators.optionalDirectoryValidator('somePrefix')

        expect:
        !validator.validate(nullFolder).present
        validator.validate(existingFile).present
        !validator.validate(existingFolder).present
        validator.validate(nonExistingFile).present
        validator.validate(nonExistingFolder).present
    }

    def "Null validator reports error without touching the target object"() {
        setup:
        def validator = Validators.nullValidator()
        def target = Mock(Object)

        when:
        def result = validator.validate(target)

        then:
        !result.isPresent()
        0 * target._ // no method was called on the target
    }
}
