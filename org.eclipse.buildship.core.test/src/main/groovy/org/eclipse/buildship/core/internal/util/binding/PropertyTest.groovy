package org.eclipse.buildship.core.internal.util.binding

import spock.lang.Specification

import com.google.common.base.Optional

class PropertyTest extends Specification {

    def "property is always created with non-null validator"() {
        setup:
        def validator = Mock(Validator)
        Property property = Property.create(validator)
        assert property != null
    }

    def "initial property value is null"() {
        setup:
        def validator = Mock(Validator)
        Property property = Property.create(validator)
        assert property.getValue() == null
    }

    def "property value is mutable"() {
        setup:
        def validator = Mock(Validator)
        Property property = Property.create(validator)
        property.setValue('alpha')
        assert property.getValue() == 'alpha'
    }

    def "property value can be set to null"() {
        setup:
        def validator = Mock(Validator)
        Property property = Property.create(validator)
        property.setValue(null)
        assert property.getValue() == null
    }

    def "validator is invoked when calling setValue"() {
        given:
        def validator = Mock(Validator)
        1 * validator.validate('alpha') >> { Optional.absent() }
        Property property = Property.create(validator)

        when:
        def errorMessage = property.setValue('alpha')

        then:
        errorMessage == Optional.absent()
    }

    def "validator is invoked when calling validate"() {
        given:
        def validator = Mock(Validator)
        1 * validator.validate(null) >> { Optional.absent() }
        Property property = Property.create(validator)

        when:
        def errorMessage = property.validate()

        then:
        errorMessage == Optional.absent()
    }

    def "validator is invoked when calling isValid"() {
        given:
        def validator = Mock(Validator)
        1 * validator.validate(null) >> { Optional.absent() }
        Property property = Property.create(validator)

        when:
        def isValid = property.isValid()

        then:
        isValid
    }

    def "validation listeners are invoked when calling setValue"() {
        given:
        def validator = Mock(Validator)
        1 * validator.validate('alpha') >> { Optional.absent() }
        Property property = Property.create(validator)

        ValidationListener listener = Mock(ValidationListener)
        1 * listener.validationTriggered(property, Optional.absent())
        property.addValidationListener(listener)

        when:
        def validate = property.setValue('alpha')

        then:
        validate == Optional.absent()
    }

    def "validation listeners are not invoked when calling validate"() {
        given:
        def validator = Mock(Validator)
        1 * validator.validate(null) >> { Optional.absent() }
        Property property = Property.create(validator)

        ValidationListener listener = Mock(ValidationListener)
        property.addValidationListener(listener)

        when:
        def validate = property.validate()

        then:
        0 * listener.validationTriggered(_, _)
        validate == Optional.absent()
    }

    def "validation listeners are not invoked when calling isValid"() {
        given:
        def validator = Mock(Validator)
        1 * validator.validate(null) >> { Optional.absent() }
        Property property = Property.create(validator)

        ValidationListener listener = Mock(ValidationListener)
        property.addValidationListener(listener)

        when:
        def isValid = property.isValid()

        then:
        0 * listener.validationTriggered(_, _)
        isValid
    }

    def "validation listeners can be unregistered"() {
        setup:
        def validator = Mock(Validator)
        Property property = Property.create(validator)
        ValidationListener listener = Mock(ValidationListener)
        property.addValidationListener(listener)
        property.removeValidationListener(listener)
        property.listeners == [] as Set
    }
}
