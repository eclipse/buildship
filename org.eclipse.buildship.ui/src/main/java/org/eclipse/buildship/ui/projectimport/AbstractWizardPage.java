/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.ui.projectimport;

import java.util.List;

import org.eclipse.buildship.ui.util.widget.UiBuilder;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Optional;
import com.gradleware.tooling.eclipse.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.HelpContext;
import org.eclipse.buildship.ui.util.font.FontUtils;
import com.gradleware.tooling.toolingutils.binding.Property;
import com.gradleware.tooling.toolingutils.binding.ValidationListener;

/**
 * Common base class for all pages in the {@link ProjectImportWizard}.
 */
public abstract class AbstractWizardPage extends WizardPage {

    private final ProjectImportConfiguration configuration;
    private final List<Property<?>> observedProperties;
    private final String defaultMessage;

    private final Font defaultFont;
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
        setImageDescriptor(ImageDescriptor.createFromFile(GradleProjectWizardPage.class, "/icons/full/wizban/import_wiz.png")); //$NON-NLS-1$

        // set up the UI builder
        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.builderFactory = new UiBuilder.UiBuilderFactory(this.defaultFont);

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
        externalRoot.setExpandHorizontal(true);
        externalRoot.setExpandVertical(true);
        externalRoot.setMinSize(new Point(230, 380));

        // add the controls inside the root composite
        Composite container = new Composite(externalRoot, SWT.NONE);
        createWidgets(container);
        externalRoot.setContent(container);

        // return the root
        return externalRoot;
    }

    protected abstract void createWidgets(Composite root);

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // every time the page becomes visible, set the proper help context, this is required since
        // the user could navigate back to the initial Eclipse import page which sets another help
        // context
        if (visible) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), HelpContext.PROJECT_IMPORT);
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

    @Override
    public void dispose() {
        this.defaultFont.dispose();
        super.dispose();
    }

}
