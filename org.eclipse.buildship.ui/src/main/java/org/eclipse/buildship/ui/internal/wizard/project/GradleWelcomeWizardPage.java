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

package org.eclipse.buildship.ui.internal.wizard.project;

import com.google.common.collect.ImmutableList;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.internal.util.binding.Property;

/**
 * Page on a {@link AbstractProjectWizard} that welcomes the user and provides some information about the wizard.
 */
public final class GradleWelcomeWizardPage extends AbstractWizardPage {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

    private final WelcomePageContent welcomePageContent;
    private final Font headerFont;

    public GradleWelcomeWizardPage(ProjectImportConfiguration configuration, WelcomePageContent welcomePageContent) {
        super(welcomePageContent.getName(), welcomePageContent.getTitle(), welcomePageContent.getMessage(),
                configuration, ImmutableList.<Property<?>> of());
        this.welcomePageContent = welcomePageContent;
        this.headerFont = createHeaderFont();
    }

    private Font createHeaderFont() {
        FontData[] fontData = JFaceResources.getDialogFont().getFontData();
        for (FontData font : fontData) {
            font.setHeight(18);
        }
        return new Font(PlatformUI.getWorkbench().getDisplay(), fontData);
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(new GridLayout(1, false));

        Composite container = new Composite(root, SWT.NONE);
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout containerLayout = new GridLayout(1, false);
        containerLayout.marginLeft = containerLayout.marginRight = 50;
        container.setLayout(containerLayout);

        StyledText welcomeText = new StyledText(container, SWT.WRAP | SWT.MULTI | SWT.CENTER);
        GridData welcomeTextLayoutData = new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1);
        welcomeTextLayoutData.widthHint = 500;
        welcomeText.setLayoutData(welcomeTextLayoutData);
        welcomeText.setBackground(welcomeText.getParent().getBackground());
        welcomeText.setEnabled(false);
        welcomeText.setEditable(false);
        fillWelcomeText(welcomeText);

        final Button showWelcomePageCheckbox = new Button(container, SWT.CHECK);
        showWelcomePageCheckbox.setText(ProjectWizardMessages.CheckButton_ShowWelcomePageNextTime);
        GridData showWelcomePageCheckboxLayoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        showWelcomePageCheckboxLayoutData.widthHint = welcomeTextLayoutData.widthHint;
        showWelcomePageCheckboxLayoutData.verticalIndent = 15;
        showWelcomePageCheckbox.setLayoutData(showWelcomePageCheckboxLayoutData);
        showWelcomePageCheckbox.setSelection(((AbstractProjectWizard) getWizard()).isShowWelcomePage());
        showWelcomePageCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                ((AbstractProjectWizard) getWizard()).setWelcomePageEnabled(showWelcomePageCheckbox.getSelection());
            }
        });
    }

    private void fillWelcomeText(StyledText welcomeText) {
        // add content title
        StringBuilder welcomeContent = new StringBuilder();
        welcomeContent.append(this.welcomePageContent.getParagraphTitle());

        // add content paragraphs
        for (WelcomePageContent.PageParagraph paragraph : this.welcomePageContent.getParagraphs()) {
            welcomeContent.append(LINE_SEPARATOR);
            welcomeContent.append(LINE_SEPARATOR);
            welcomeContent.append(paragraph.getTitle());
            welcomeContent.append(paragraph.getContent());
        }

        // justify content text
        welcomeText.setText(welcomeContent.toString());
        welcomeText.setLineJustify(1, welcomeText.getLineCount() - 1, true);

        // make content title and paragraph titles bold
        setBoldTitle(welcomeText, this.welcomePageContent.getParagraphTitle(), this.headerFont);
        for (WelcomePageContent.PageParagraph welcomePageParagraph : this.welcomePageContent.getParagraphs()) {
            setBoldTitle(welcomeText, welcomePageParagraph.getTitle(), null);
        }
    }

    private void setBoldTitle(StyledText welcomeText, String title, Font font) {
        StyleRange titleStyle = new StyleRange();
        titleStyle.start = welcomeText.getText().indexOf(title);
        titleStyle.length = title.length();
        titleStyle.font = font;
        titleStyle.fontStyle = SWT.BOLD;
        welcomeText.setStyleRange(titleStyle);
    }

    @Override
    protected String getPageContextInformation() {
        return this.welcomePageContent.getPageContextInformation();
    }

    @Override
    public void dispose() {
        this.headerFont.dispose();
        super.dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        // if the welcome page is visible, disable the Finish button
        ((AbstractProjectWizard) getWizard()).setFinishGloballyEnabled(!visible);
    }

}
