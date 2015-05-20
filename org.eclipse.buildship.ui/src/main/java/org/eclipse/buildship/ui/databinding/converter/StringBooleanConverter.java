package org.eclipse.buildship.ui.databinding.converter;

import org.eclipse.core.databinding.conversion.Converter;


public class StringBooleanConverter extends Converter {

    public StringBooleanConverter() {
        super(null, null);
    }

    @Override
    public Object convert(Object fromObject) {
        if (fromObject instanceof String) {
            return Boolean.parseBoolean((String) fromObject);
        } else if (fromObject instanceof Boolean) {
            return fromObject.toString();
        }
        return null;
    }

}
