package org.eclipse.buildship.ui.preferences;

import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;

/*
 * @author Benjamin gurok
 * @author Paul Verest
 */
public class GradleEditorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = UiPlugin.getInstance().getPreferenceStore();
        PreferenceConverter.setDefault(store, GradleEditorConstants.KEY_COLOR_COMMENT, GradleEditorConstants.DEFAULT_COLOR_COMMENT);
        PreferenceConverter.setDefault(store, GradleEditorConstants.KEY_COLOR_DOC, GradleEditorConstants.DEFAULT_COLOR_DOC);
        PreferenceConverter.setDefault(store, GradleEditorConstants.KEY_COLOR_KEYWORD, GradleEditorConstants.DEFAULT_COLOR_KEYWORD);
        PreferenceConverter.setDefault(store, GradleEditorConstants.KEY_COLOR_STRING, GradleEditorConstants.DEFAULT_COLOR_STRING);
        PreferenceConverter.setDefault(store, GradleEditorConstants.KEY_COLOR_NUMBER, GradleEditorConstants.DEFAULT_COLOR_NUMBER);
        PreferenceConverter.setDefault(store, GradleEditorConstants.KEY_COLOR_NORMAL, GradleEditorConstants.DEFAULT_COLOR_NORMAL);
        store.setDefault(GradleEditorConstants.KEY_BOLD_KEYWORD, true);	}

}

