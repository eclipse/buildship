/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 *     Sebastian Kuzniarz (Diebold Nixdorf Inc.) - refactored HelpContextIdProvider
 */

package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import org.eclipse.buildship.ui.internal.util.font.FontUtils;
import org.eclipse.buildship.ui.internal.util.widget.UiBuilder;
import org.eclipse.buildship.ui.internal.wizard.HelpContextIdProvider;
import org.eclipse.buildship.ui.internal.wizard.project.ProjectImportWizard;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.google.common.base.Strings;

/**
 * Common base class for all pages in the {@link ProjectImportWizard}.
 */
public abstract class AbstractWizardPage extends WizardPage {

	@SuppressWarnings("unused")
	private final String defaultMessage;
	private final Font defaultFont;
	private final UiBuilder.UiBuilderFactory builderFactory;

    /**
     * Constructor setting up the main messages and the validation facility for this wizard page.
     *
     * @param name the name of the page
     * @param title the page title
     * @param defaultMessage the default message to show when there is no validation error
     */
    protected AbstractWizardPage(String name, String title, String defaultMessage) {
        super(name);

        this.defaultMessage = defaultMessage;

        // set the basic message and the attached image
        setTitle(title);
        setMessage(defaultMessage);
        setImageDescriptor(ImageDescriptor.createFromFile(GradleCreateWorkspaceCompositeWizardPage.class, "/icons/full/wizban/wizard.png")); //$NON-NLS-1$

        // set up the UI builder
        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.builderFactory = new UiBuilder.UiBuilderFactory(this.defaultFont);

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

        // add context information to the bottom of the page if the page defines it
        String contextInformation = Strings.emptyToNull(getPageContextInformation());
        if (contextInformation != null) {
            createWidgetsForContextInformation(container, contextInformation);
        }

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

    private void createWidgetsForContextInformation(Composite root, String contextInformation) {
        // create a container box occupying all horizontal space and has a 1-column row layout and a
        // 30 pixel margin to not stretch the separator widgets to the edge of the wizard page
        Composite contextInformationContainer = new Composite(root, SWT.NONE);
        GridLayout contextInformationContainerLayout = new GridLayout(1, false);
        contextInformationContainerLayout.marginLeft = contextInformationContainerLayout.marginRight = contextInformationContainerLayout.marginTop = 30;
        contextInformationContainerLayout.verticalSpacing = 15;
        contextInformationContainer.setLayout(contextInformationContainerLayout);
        contextInformationContainer.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 3, 1));

        // separator widget
        Label separator = new Label(contextInformationContainer, SWT.HORIZONTAL | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));

        // internal container for flexible resize
        Composite textContainer = new Composite(contextInformationContainer, SWT.NONE);
        textContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        GridLayout textContainerLayout = new GridLayout(1, false);
        textContainerLayout.marginLeft = textContainerLayout.marginRight = 50;
        textContainer.setLayout(textContainerLayout);

        // text widget aligned to the center having 400 pixels allocated for each line of content
        StyledText contextInformationText = new StyledText(textContainer, SWT.WRAP | SWT.MULTI | SWT.CENTER);
        contextInformationText.setText(contextInformation);
        contextInformationText.setBackground(contextInformationText.getParent().getBackground());
        contextInformationText.setEnabled(false);
        contextInformationText.setEditable(false);
        GridData contextInformationTextLayoutData = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        contextInformationTextLayoutData.widthHint = 400;
        contextInformationText.setLayoutData(contextInformationTextLayoutData);
    }

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
    public boolean isPageComplete() {
        return true;
    }

    @Override
    public void dispose() {
        this.defaultFont.dispose();
        super.dispose();
    }

}
