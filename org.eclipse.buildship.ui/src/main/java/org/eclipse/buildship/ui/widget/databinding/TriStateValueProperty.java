package org.eclipse.buildship.ui.widget.databinding;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;

import org.eclipse.buildship.ui.widget.TriState;


public class TriStateValueProperty extends WidgetValueProperty {

    public TriStateValueProperty() {
        super(SWT.Selection);
    }

    @Override
    public Object getValueType() {
        return null;
    }

    @Override
    protected Object doGetValue(Object source) {
        if (source instanceof TriState) {
            return ((TriState) source).getState();
        }

        return null;
    }

    @Override
    protected void doSetValue(Object source, Object value) {
        if (source instanceof TriState) {
            ((TriState) source).setState((Boolean) value);
        }
    }

}
