package org.eclipse.buildship.ui.wizard.task.renderer;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;


public class StringPropertyRenderer implements PropertyRenderer {

    private Text propertyValue;

    @Override
    public void createControl(Composite parent) {
        propertyValue = new Text(parent, SWT.BORDER);
        propertyValue.setMessage("Set the value of this property");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(propertyValue);
    }

    @Override
    public Control getControl() {
        return propertyValue;
    }

    @Override
    public IObservableValue getObservable() {
        return WidgetProperties.text(SWT.Modify).observe(propertyValue);
    }

    @Override
    public UpdateValueStrategy getTargetUpdateValueStrategy() {
        // Not necessary here
        return null;
    }

    @Override
    public UpdateValueStrategy getModelUpdateValueStrategy() {
        // Not necessary here
        return null;
    }

}
