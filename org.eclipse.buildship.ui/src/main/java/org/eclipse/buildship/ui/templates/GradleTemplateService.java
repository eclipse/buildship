package org.eclipse.buildship.ui.templates;

import java.io.IOException;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.editor.GradleEditor;

public class GradleTemplateService {

    private static final String TEMPLATES_KEY = "org.eclipse.buildship.ui.templates";

    private ContextTypeRegistry contextTypeRegistry;

    private TemplateStore codeTemplateStore;

    public TemplateStore getTemplateStore() {
        if (codeTemplateStore == null) {
            IPreferenceStore store = UiPlugin.getInstance().getPreferenceStore();
            codeTemplateStore = new ContributionTemplateStore(getTemplateContextRegistry(), store, TEMPLATES_KEY);

            try {
                codeTemplateStore.load();
            } catch (IOException e) {
                UiPlugin.logger().error(e.getMessage(), e);
            }

            codeTemplateStore.startListeningForPreferenceChanges();
        }

        return codeTemplateStore;
    }

    /**
     * Returns the template context type registry for the gradle plug-in.
     *
     * @return the template context type registry for the gradle plug-in
     */
    public ContextTypeRegistry getTemplateContextRegistry() {
        if (contextTypeRegistry == null) {
            ContributionContextTypeRegistry registry = new ContributionContextTypeRegistry(GradleEditor.ID);

            contextTypeRegistry = registry;
        }

        return contextTypeRegistry;
    }
}
