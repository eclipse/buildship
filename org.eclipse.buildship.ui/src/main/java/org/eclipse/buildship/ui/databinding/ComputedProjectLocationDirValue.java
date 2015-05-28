/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.databinding;

import java.io.File;

import com.google.common.base.Preconditions;

import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * This {@link ComputedValue} computes a {@link File} according to the given observables.
 *
 */
public class ComputedProjectLocationDirValue extends ComputedValue {

    private IObservableValue projectName;
    private IObservableValue useDefaultLocation;
    private IObservableValue alternativeLocation;

    /**
     * Constructor for this project location value.
     *
     * @param projectName The name of the Project (has to contain a {@link String})
     * @param useDefaultLocation Indicates whether to use the default workspace location or the
     *            alternativeLocation (has to contain a {@link Boolean})
     * @param alternativeLocation The custom path to the project (has to contain a {@link String})
     */
    public ComputedProjectLocationDirValue(IObservableValue projectName, IObservableValue useDefaultLocation, IObservableValue alternativeLocation) {
        this.projectName = Preconditions.checkNotNull(projectName);
        this.useDefaultLocation = Preconditions.checkNotNull(useDefaultLocation);
        this.alternativeLocation = Preconditions.checkNotNull(alternativeLocation);
    }

    @Override
    protected Object calculate() {
        if ((Boolean) this.useDefaultLocation.getValue()) {
            IPath location = ResourcesPlugin.getWorkspace().getRoot().getLocation();
            return new File(location.toOSString(), (String) this.projectName.getValue());
        }
        return new File((String) this.alternativeLocation.getValue(), (String) this.projectName.getValue());
    }

}
