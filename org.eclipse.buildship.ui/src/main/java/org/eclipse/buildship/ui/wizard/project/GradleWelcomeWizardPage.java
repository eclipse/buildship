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

package org.eclipse.buildship.ui.wizard.project;

import java.util.List;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.ui.wizard.project.WelcomePageContent.WelcomePageParagraph;
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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.gradleware.tooling.toolingutils.binding.Property;

/**
 * Abstract Welcome Page for displaying a welcome message.
 */
public class GradleWelcomeWizardPage extends AbstractWizardPage {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$

    private final Font headerFont;
    private WelcomePageContent welcomePageConfigurator;

    public GradleWelcomeWizardPage(ProjectImportConfiguration configuration,
            WelcomePageContent welcomePageConfigurator) {
        super(welcomePageConfigurator.getName(), welcomePageConfigurator.getTitle(),
                welcomePageConfigurator.getMessage(), configuration, ImmutableList.<Property<?>> of());
        this.welcomePageConfigurator = Preconditions.checkNotNull(welcomePageConfigurator);
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

        StyledText welcomeText = new StyledText(root, SWT.WRAP | SWT.MULTI | SWT.CENTER);
        GridData welcomeTextLayoutData = new GridData(SWT.CENTER, SWT.TOP, false, false, 1, 1);
        welcomeTextLayoutData.widthHint = 500;
        welcomeText.setLayoutData(welcomeTextLayoutData);
        welcomeText.setBackground(welcomeText.getParent().getBackground());
        welcomeText.setEnabled(false);
        welcomeText.setEditable(false);
        fillWelcomeText(welcomeText);

        final Button showWelcomePageCheckbox = new Button(root, SWT.CHECK);
        showWelcomePageCheckbox.setText(ProjectWizardMessages.CheckButton_Show_Welcomepage_Next_Time);
        GridData showWelcomePageCheckboxLayoutData = new GridData(SWT.CENTER, SWT.BOTTOM, false, false, 1, 1);
        showWelcomePageCheckboxLayoutData.widthHint = welcomeTextLayoutData.widthHint;
        showWelcomePageCheckboxLayoutData.verticalIndent = 15;
        showWelcomePageCheckbox.setLayoutData(showWelcomePageCheckboxLayoutData);
        showWelcomePageCheckbox.setSelection(((AbstractProjectWizard) getWizard()).isShowWelcomePage());
        showWelcomePageCheckbox.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {

                if (getWizard() instanceof AbstractProjectWizard) {

                }
            }
        });
    }

    private void fillWelcomeText(StyledText welcomeText) {
        StringBuilder welcomeContent = new StringBuilder();
        welcomeContent.append(this.welcomePageConfigurator.getWelcomePageParagraphTitle());

        List<WelcomePageParagraph> welcomePageParagraphs = this.welcomePageConfigurator
                .getWelcomePageParagraphs();

        for (WelcomePageParagraph welcomePageParagraph : welcomePageParagraphs) {
            String title = welcomePageParagraph.getTitle();
            String content = welcomePageParagraph.getContent();

            welcomeContent.append(LINE_SEPARATOR);
            welcomeContent.append(LINE_SEPARATOR);
            welcomeContent.append(title);
            welcomeContent.append(content);
        }


        // justify paragraph text
        welcomeText.setText(welcomeContent.toString());
        welcomeText.setLineJustify(1, welcomeText.getLineCount() - 1, true);

        // make titles bold
        setBoldTitle(welcomeText, this.welcomePageConfigurator.getWelcomePageParagraphTitle(), this.headerFont);

        for (WelcomePageParagraph welcomePageParagraph : welcomePageParagraphs) {
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
        return this.welcomePageConfigurator.getPageContextInformation();
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
