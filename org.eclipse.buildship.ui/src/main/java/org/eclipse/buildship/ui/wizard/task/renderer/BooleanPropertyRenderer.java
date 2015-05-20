package org.eclipse.buildship.ui.wizard.task.renderer;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.buildship.ui.databinding.converter.StringBooleanConverter;
import org.eclipse.buildship.ui.widget.TriStateWidget;
import org.eclipse.buildship.ui.widget.databinding.TriStateValueProperty;


public class BooleanPropertyRenderer implements PropertyRenderer {

    private TriStateWidget propertyValue;
    private StringBooleanConverter stringBooleanConverter;

    public BooleanPropertyRenderer() {
        this.stringBooleanConverter = new StringBooleanConverter();
    }

    @Override
    public void createControl(Composite parent) {
        propertyValue = new TriStateWidget(parent, SWT.CHECK);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(propertyValue);
    }

    @Override
    public Control getControl() {
        return propertyValue;
    }

    @Override
    public IObservableValue getObservable() {
        TriStateValueProperty triStateValueProperty = new TriStateValueProperty();
        return triStateValueProperty.observe(propertyValue);
    }

    @Override
    public UpdateValueStrategy getTargetUpdateValueStrategy() {
        UpdateValueStrategy updateStrategy = new UpdateValueStrategy();
        updateStrategy.setConverter(stringBooleanConverter);

        return updateStrategy;
    }

    @Override
    public UpdateValueStrategy getModelUpdateValueStrategy() {
        UpdateValueStrategy updateStrategy = new UpdateValueStrategy();
        updateStrategy.setConverter(stringBooleanConverter);

        return updateStrategy;
    }

}
