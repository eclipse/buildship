/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.ui.internal.util.widget;

import java.io.File;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import org.eclipse.buildship.core.internal.i18n.CoreMessages;
import org.eclipse.buildship.ui.internal.i18n.UiMessages;
import org.eclipse.buildship.ui.internal.util.file.DirectoryDialogSelectionListener;
import org.eclipse.buildship.ui.internal.util.font.FontUtils;

/**
 * Composite to select the Gradle user home.
 *
 * @author Donat Csikos
 */
public final class GradleUserHomeGroup extends Group {

    private final Font defaultFont;
    private final UiBuilder.UiBuilderFactory builderFactory;

    private Text gradleUserHomeText;
    private Button gradleUserHomeBrowseButton;
    private Label warningLabel;

    public GradleUserHomeGroup(Composite parent) {
        super(parent, SWT.NONE);
        setText(CoreMessages.Preference_Label_GradleUserHome);

        this.defaultFont = FontUtils.getDefaultDialogFont();
        this.builderFactory = new UiBuilder.UiBuilderFactory(this.defaultFont);

        setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        setLayout(new GridLayout(3, false));

        this.gradleUserHomeText = this.builderFactory.newText(this).alignFillHorizontal().control();
        this.gradleUserHomeBrowseButton = this.builderFactory.newButton(this).alignLeft().text(UiMessages.Button_Label_Browse).control();

        this.warningLabel = this.builderFactory.newLabel(this).alignLeft().control();
        this.warningLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
        HoverText.createAndAttach(this.warningLabel, NLS.bind(CoreMessages.WarningMessage_Using_0_NonPortable, "Gradle user home"));

        addListeners();
    }

    private void addListeners() {
        this.gradleUserHomeBrowseButton
                .addSelectionListener(new DirectoryDialogSelectionListener(this.getShell(), this.gradleUserHomeText, CoreMessages.Preference_Label_GradleUserHome));
    }

    public Text getGradleUserHomeText() {
        return this.gradleUserHomeText;
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateEnablement();
    }

    private void updateEnablement() {
        boolean groupEnabled = getEnabled();
        this.gradleUserHomeText.setEnabled(groupEnabled);
        this.gradleUserHomeBrowseButton.setEnabled(groupEnabled);
    }

    public File getGradleUserHome() {
        String gradleUserHomeString = this.gradleUserHomeText.getText();
        return gradleUserHomeString.isEmpty() ? null : new File(gradleUserHomeString);
    }

    public void setGradleUserHome(File gradleUserHome) {
        if (gradleUserHome == null) {
            this.gradleUserHomeText.setText("");
        } else {
            this.gradleUserHomeText.setText(gradleUserHome.getPath());
        }
    }

    @Override
    public void dispose() {
        if (this.defaultFont != null && !this.defaultFont.isDisposed()) {
            this.defaultFont.dispose();
        }
        super.dispose();
    }
}
