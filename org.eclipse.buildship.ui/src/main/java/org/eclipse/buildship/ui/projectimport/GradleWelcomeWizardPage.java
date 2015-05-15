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

import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingutils.binding.Property;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * First page in the {@link ProjectImportWizard} displaying a welcome message.
 */
public final class GradleWelcomeWizardPage extends AbstractWizardPage {

    private final Font headerFont;

    public GradleWelcomeWizardPage() {
        super("Welcome", ProjectImportMessages.Title_GradleWelcomeWizardPage, "", null, ImmutableList.<Property<?>> of()); //$NON-NLS-1$
        this.headerFont = createHeaderFont();
    }

    private Font createHeaderFont() {
        FontData[] fontData = JFaceResources.getDialogFont().getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setHeight(18);
        }
        Font headerFont = new Font(Display.getDefault(), fontData);
        return headerFont;
    }

    @Override
    protected void createWidgets(Composite root) {
        GridLayout layout = new GridLayout(1, false);
        layout.marginTop = 20; // space above the controls
        root.setLayout(layout);

        StyledText welcomeText = new StyledText(root, SWT.WRAP | SWT.MULTI | SWT.CENTER);
        GridData welcomeLayoutData = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        welcomeLayoutData.widthHint = 500;
        welcomeText.setLayoutData(welcomeLayoutData);
        welcomeText.setBackground(welcomeText.getParent().getBackground());
        welcomeText.setEnabled(false);
        welcomeText.setEditable(false);
        fillWelcomeText(welcomeText);
    }

    private void fillWelcomeText(StyledText welcome) {
        String headerText = "Welcome to the Gradle project import wizard";
        String welcomeText = headerText + "\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit. "
                + "Donec a diam lectus. Sed sit amet ipsum mauris. Maecenas congue ligula ac quam viverra nec "
                + "consectetur ante hendrerit. Donec et mollis dolor.\n\nVivamus fermentum semper porta. Nunc"
                + " diam velit, adipiscing ut tristique vitae, sagittis vel odio. Maecenas convallis ullamcorper"
                + " ultricies. Curabitur ornare, ligula semper consectetur sagittis, nisi diam iaculis velit, "
                + "id fringilla sem nunc vel mi. Nam dictum, odio nec pretium volutpat, arcu ante placerat erat, non tristique.";
        welcome.setText(welcomeText);

        StyleRange headerStyle = new StyleRange();
        headerStyle.start = 0;
        headerStyle.length = headerText.length();
        headerStyle.font = this.headerFont;
        headerStyle.fontStyle = SWT.BOLD;
        welcome.setStyleRange(headerStyle);
    }

    @Override
    protected String getPageContextInformation() {
        return ProjectImportMessages.InfoMessage_GradleWelcomeWizardPageContext;
    }

    @Override
    public void dispose() {
        this.headerFont.dispose();
        super.dispose();
    }

}
