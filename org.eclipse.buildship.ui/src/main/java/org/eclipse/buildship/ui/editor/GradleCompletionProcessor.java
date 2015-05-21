/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 465728
 */

package org.eclipse.buildship.ui.editor;

import java.util.List;

import com.google.common.collect.Lists;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.editor.completionproposals.GradleTaskCompletionProposal;
import org.eclipse.buildship.ui.templates.GradleTemplateContextType;

/**
 * {@link IContentAssistProcessor} for the {@link GradleEditor}.
 *
 */
public class GradleCompletionProcessor extends TemplateCompletionProcessor implements IContentAssistProcessor {

    private Shell shell;

    public GradleCompletionProcessor(Shell shell) {
        this.shell = shell;
    }

    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

        ICompletionProposal[] computeCompletionProposals = super.computeCompletionProposals(viewer, offset);

        List<ICompletionProposal> completionProposals = Lists.asList(new GradleTaskCompletionProposal(shell, offset), computeCompletionProposals);

        return completionProposals.toArray(new ICompletionProposal[completionProposals.size()]);
    }

    @Override
    protected Template[] getTemplates(String contextTypeId) {
        TemplateStore templateStore = UiPlugin.templateService().getTemplateStore();
        return templateStore.getTemplates();
    }

    @Override
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
        return new GradleTemplateContextType();
    }

    @Override
    protected Image getImage(Template template) {
        return PluginImages.TEMPLATE.withState(ImageState.ENABLED).getImage();
    }

}