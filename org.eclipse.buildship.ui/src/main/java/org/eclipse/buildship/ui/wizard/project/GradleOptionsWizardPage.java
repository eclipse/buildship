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

import com.google.common.collect.ImmutableList;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingutils.binding.Property;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper;
import org.eclipse.buildship.core.util.gradle.PublishedGradleVersionsWrapper;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup;
import org.eclipse.buildship.ui.util.widget.GradleDistributionGroup.DistributionChangedListener;

/**
 * Page on the {@link ProjectImportWizard} declaring the used Gradle distribution and other advanced options for the imported project.
 */
public final class GradleOptionsWizardPage extends AbstractWizardPage {

    private final PublishedGradleVersionsWrapper publishedGradleVersions;
    private final String pageContextInformation;

    public GradleOptionsWizardPage(ProjectImportConfiguration configuration, PublishedGradleVersionsWrapper publishedGradleVersions) {
        this(configuration, publishedGradleVersions, ProjectWizardMessages.Title_GradleOptionsWizardPage, ProjectWizardMessages.InfoMessage_GradleOptionsWizardPageDefault,
                ProjectWizardMessages.InfoMessage_GradleOptionsWizardPageContext);
    }

    public GradleOptionsWizardPage(ProjectImportConfiguration configuration, PublishedGradleVersionsWrapper publishedGradleVersions, String title, String defaultMessage, String pageContextInformation) {
        super("GradleOptions", title, defaultMessage, configuration, ImmutableList.<Property<?>>of(configuration.getGradleDistribution()));
        this.publishedGradleVersions = publishedGradleVersions;
        this.pageContextInformation = pageContextInformation;
    }

    @Override
    protected void createWidgets(Composite root) {
        root.setLayout(new GridLayout(1, false));
        GradleDistributionGroup gradleDistributionGroup = new GradleDistributionGroup(this.publishedGradleVersions, root);
        gradleDistributionGroup.setGradleDistribution(getConfiguration().getGradleDistribution().getValue());
        gradleDistributionGroup.addDistributionChangedListener(new DistributionChangedListener() {

            @Override
            public void distributionUpdated(GradleDistributionWrapper distribution) {
                getConfiguration().setGradleDistribution(distribution);
            }
        });
    }

    @Override
    protected String getPageContextInformation() {
        return this.pageContextInformation;
    }
}
