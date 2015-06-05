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

package org.eclipse.buildship.ui.wizard.project;

import java.io.File;
import java.util.List;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.buildship.core.launch.GradleRunConfigurationAttributes;
import org.eclipse.buildship.core.project.ConvertToGradleProjectJob;
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;

/**
 * This class offers a method, which opens {@link GradleProjectConvertWizard} and runs the
 * {@link ConvertToGradleProjectJob}.
 *
 */
public class GradleProjectConvert {

    public IStatus convertProject(IProject project, Shell shell) {

        GradleProjectConvertWizard projectConvertWizard = new GradleProjectConvertWizard();
        WizardDialog wizardDialog = new WizardDialog(shell, projectConvertWizard);
        if (Window.OK == wizardDialog.open()) {
            ProjectImportWizardController controller = projectConvertWizard.getController();
            ProjectImportConfiguration configuration = controller.getConfiguration();
            File projectLocationFile = project.getLocation().toFile();
            configuration.setProjectDir(projectLocationFile);

            FixedRequestAttributes fixedAttributes = configuration.toFixedAttributes();
            GradleRunConfigurationAttributes configurationAttributes = GradleRunConfigurationAttributes.with(ImmutableList.<String> of(), projectLocationFile.getAbsolutePath(),
                    fixedAttributes.getGradleDistribution(), fixedAttributes.getGradleUserHome() != null ? fixedAttributes.getGradleUserHome().getAbsolutePath() : null,
                    fixedAttributes.getJavaHome() != null ? fixedAttributes.getJavaHome().getAbsolutePath() : null,
                    Optional.<List<String>> fromNullable(fixedAttributes.getJvmArguments()).orNull(),
                    Optional.<List<String>> fromNullable(fixedAttributes.getArguments()).orNull(), true, true);
            ConvertToGradleProjectJob convertToGradleProjectJob = new ConvertToGradleProjectJob(configurationAttributes, project);
            convertToGradleProjectJob.schedule();
        } else {
            Bundle bundle = FrameworkUtil.getBundle(getClass());
            return new Status(IStatus.CANCEL, bundle.getSymbolicName(), "Canceled Project Conversion");
        }

        return Status.OK_STATUS;
    }

}
