/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.preferences;

import java.util.List;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.ui.internal.wizard.HelpContextIdProvider;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.CompositeConfiguration;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.GradleCreateWorkspaceCompositeWizardPage;
import org.eclipse.buildship.ui.internal.wizard.workspacecomposite.WorkspaceCompositeCreationWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

/**
 * Common base class for all pages in the {@link WorkspaceCompositeCreationWizard}.
 */
public abstract class AbstractPropertiesPage extends WizardPage {

    private final CompositeConfiguration configuration;

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
    protected AbstractPropertiesPage(String name, String title, String defaultMessage, CompositeConfiguration configuration, final List<Property<?>> observedProperties) {
        super(name);

        this.configuration = configuration;

        // set the basic message and the attached image
        setTitle(title);
        setDescription(defaultMessage);
        setImageDescriptor(ImageDescriptor.createFromFile(GradleCreateWorkspaceCompositeWizardPage.class, "/icons/full/wizban/wizard.png")); //$NON-NLS-1$
    }

    protected CompositeConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void createControl(Composite parent) {
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

        // also compute the size of the container, otherwise the ScrolledComposite's content is not
        // rendered properly
        Point containerSize = container.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        container.setSize(containerSize);

        // set the root's content and return it
        externalRoot.setContent(container);
        return externalRoot;
    }

    /**
     * Populates the widgets in the wizard page.
     */
    protected abstract void createWidgets(Composite root);

    /**
     * Returns text to display under the widgets. If {@code null} or empty then nothing is displayed.
     *
     * @return explanation text for for the wizard page
     */
    protected abstract String getPageContextInformation();

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
    public void dispose() {
        super.dispose();
    }

}
