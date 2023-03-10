/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc. and others
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.ui.internal.wizard.project;

import java.util.List;

import com.google.common.base.Optional;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.ValidationListener;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder;

/**
 * Common base class for all pages in the {@link ProjectImportWizard}.
 */
public abstract class AbstractWizardPage extends WizardPage {

    private final ProjectImportConfiguration configuration;
    private final List<Property<?>> observedProperties;
    private final String defaultMessage;

    private final UiBuilder.UiBuilderFactory builderFactory;

    /**
     * Constructor setting up the main messages and the validation facility for this wizard page.
     *
     * @param name the name of the page
     * @param title the page title
     * @param defaultMessage the default message to show when there is no validation error
     * @param configuration the data model of the wizard
     * @param observedProperties the subset of the properties from the data model that are managed
     *            on this page
     */
    protected AbstractWizardPage(String name, String title, String defaultMessage, ProjectImportConfiguration configuration, final List<Property<?>> observedProperties) {
        super(name);

        this.configuration = configuration;
        this.observedProperties = observedProperties;
        this.defaultMessage = defaultMessage;

        // set the basic message and the attached image
        setTitle(title);
        setMessage(defaultMessage);
        setImageDescriptor(ImageDescriptor.createFromFile(GradleProjectWizardPage.class, "/icons/full/wizban/wizard.png")); //$NON-NLS-1$

        // set up the UI builder
        this.builderFactory = new UiBuilder.UiBuilderFactory(JFaceResources.getDialogFont());

        // create a listener that updates the state and the message if an observed property in the
        // model changes
        ValidationListener listener = new ValidationListener() {

            @Override
            public void validationTriggered(Property<?> source, Optional<String> validationErrorMessage) {
                // if the modified property is invalid, show its error message, otherwise check if
                // any of the other properties of this page is invalid and if so, display the first
                // found error message
                if (validationErrorMessage.isPresent()) {
                    setMessage(validationErrorMessage.get(), IMessageProvider.ERROR);
                } else {
                    Optional<String> otherErrorMessage = validateAllObservedProperties();
                    if (!otherErrorMessage.isPresent()) {
                        setMessage(AbstractWizardPage.this.defaultMessage);
                    } else {
                        setMessage(otherErrorMessage.get(), IMessageProvider.ERROR);
                    }
                }

                // we set the page to completed if all its properties are valid
                setPageComplete(isPageComplete());
            }

            private Optional<String> validateAllObservedProperties() {
                for (Property<?> property : observedProperties) {
                    Optional<String> errorMessage = property.validate();
                    if (errorMessage.isPresent()) {
                        return errorMessage;
                    }
                }
                return Optional.absent();
            }
        };

        // attach the listener to all of the observed properties
        for (Property<?> property : observedProperties) {
            property.addValidationListener(listener);
        }
    }

    protected ProjectImportConfiguration getConfiguration() {
        return this.configuration;
    }

    protected UiBuilder.UiBuilderFactory getUiBuilderFactory() {
        return this.builderFactory;
    }

    @Override
    public final void createControl(Composite parent) {
        // align dialog units to the current resolution
        initializeDialogUnits(parent);

        // create the container control
        Composite pageControl = createWizardPageContent(parent);

        // assign the created control to the wizard page
        setControl(pageControl);
    }

    private Composite createWizardPageContent(Composite parent) {
        // create a scrollable root to handle resizing
        ScrolledComposite externalRoot = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

        // add the controls inside the root composite
        Composite container = new Composite(externalRoot, SWT.NONE);
        createWidgets(container);

        Point preferredSize = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        externalRoot.setExpandHorizontal(true);
        externalRoot.setExpandVertical(true);
        externalRoot.setMinSize(preferredSize);

        // set the root's content and return it
        externalRoot.setContent(container);
        return externalRoot;
    }

    /**
     * Populates the widgets in the wizard page.
     */
    protected abstract void createWidgets(Composite root);

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // every time the page becomes visible, set the proper help context, this is required since
        // the user could navigate back to the initial Eclipse import page which sets another help
        // context
        if (visible) {
            if (getWizard() instanceof HelpContextIdProvider) {
                String helpContextId = ((HelpContextIdProvider) getWizard()).getHelpContextId();
                PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), helpContextId);
            }
        }
    }

    @Override
    public boolean isPageComplete() {
        for (Property<?> property : this.observedProperties) {
            if (!property.isValid()) {
                return false;
            }
        }
        return true;
    }

}
