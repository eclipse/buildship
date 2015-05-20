package org.eclipse.buildship.ui.wizard.task.renderer;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface PropertyRenderer {

    void createControl(Composite parent);

    Control getControl();

    IObservableValue getObservable();

    UpdateValueStrategy getTargetUpdateValueStrategy();

    UpdateValueStrategy getModelUpdateValueStrategy();
}
