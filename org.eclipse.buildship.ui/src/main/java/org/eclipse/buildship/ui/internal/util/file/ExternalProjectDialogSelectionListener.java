/*******************************************************************************
 * Copyright (c) 2020 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.buildship.ui.internal.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.common.base.Preconditions;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.buildship.ui.internal.i18n.UiMessages;

public class ExternalProjectDialogSelectionListener extends SelectionAdapter {

    private final Shell shell;
    private final TreeViewer projectTreeViewer;
    private final String title;
    private final Map<String, String> externalProjectPaths;

    public ExternalProjectDialogSelectionListener(Shell shell, TreeViewer treeViewer, String entity) {

        this.shell = Preconditions.checkNotNull(shell);
        this.projectTreeViewer = treeViewer;
        this.title = NLS.bind(UiMessages.Title_Select_0, entity);
        ColumnViewerToolTipSupport.enableFor(this.projectTreeViewer);
        this.externalProjectPaths = new HashMap<>();
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        DirectoryDialog directoryDialog = new DirectoryDialog(this.shell, SWT.SHEET);
        directoryDialog.setText(this.title);

        String userHomeDir = System.getProperty("user.home");
        directoryDialog.setFilterPath(userHomeDir);

        String selectedDirectory = directoryDialog.open();
        if (selectedDirectory != null) {
            addExternalProjectToProjectTree(selectedDirectory);
        }
    }

    private void addExternalProjectToProjectTree(String selectedDirectory) {
        String projectDir = selectedDirectory;
        File gradleSettingsFile = getGradleSettings(projectDir);
        if (gradleSettingsFile.isFile()) {
            try {
                FileInputStream inputStream = new FileInputStream(gradleSettingsFile);
                Properties gradleSettings = new Properties();
                gradleSettings.load(inputStream);
                String projectName = getProjectName(gradleSettings);
                TreeItem jItem = new TreeItem(this.projectTreeViewer.getTree(), 0);
                jItem.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
                jItem.setChecked(true);
                jItem.setText(projectName + " (External): " + gradleSettingsFile.getParentFile().getPath());
                if (!this.externalProjectPaths.containsKey(gradleSettingsFile.getParentFile().getPath())) {
                    this.externalProjectPaths.put(gradleSettingsFile.getParentFile().getPath(), projectName);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            MessageBox dialog = new MessageBox(this.shell, SWT.ICON_ERROR | SWT.OK);
            dialog.setText("Error");
            dialog.setMessage("The selected directory is not a gradle project dir!");
            dialog.open();
        }
    }

    private String getProjectName(Properties gradleSettings) {
        //Refactored method to include String cleaning
        return gradleSettings.get("rootProject.name").toString().replaceAll("'", "").replaceAll("\"", "");
    }

    private File getGradleSettings(String projectDir) {
        File groovyFile = new File(projectDir + "\\settings.gradle");
        File kotlinFile = new File(projectDir + "\\settings.gradle.kts");
        if (groovyFile.exists()) {
            return groovyFile;
        } else if (kotlinFile.exists()) {
            return kotlinFile;
        } else {
            return new File("");
        }
    }

    public Map<String, String> getExternalProjectPaths() {
        return this.externalProjectPaths;
    }

}
