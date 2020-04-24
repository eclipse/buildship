package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

import java.io.File;

import org.eclipse.buildship.core.internal.util.binding.Property;
import org.eclipse.buildship.core.internal.util.binding.Validators;
import org.eclipse.buildship.core.internal.util.file.FileUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;

import com.google.common.base.Optional;

public class CompositeRootProjectWizardController {

	 private static final String SETTINGS_KEY_USE_COMPOSITE_ROOT = "use_composite_root"; //$NON-NLS-1$
	    private static final String SETTINGS_KEY_COMPOSITE_ROOT_PROJECT = "composite_root_project"; //$NON-NLS-1$

		private final CompositeRootProjectConfiguration configuration;
		
		public CompositeRootProjectWizardController(IWizard compositeCreationWizard) {
	        // assemble configuration object that serves as the data model of the wizard
	        Property<Boolean> compositeNameProperty = Property.create(Validators.<Boolean>nullValidator());
	        Property<File> compositeProjectsProperty = Property.create(Validators.nonExistentDirectoryValidator(WorkspaceCompositeWizardMessages.Label_RootProject));

			this.configuration = new CompositeRootProjectConfiguration(compositeNameProperty, compositeProjectsProperty);
			
			IDialogSettings dialogSettings = compositeCreationWizard.getDialogSettings();
			Boolean useCompositeRoot = dialogSettings.getBoolean(SETTINGS_KEY_USE_COMPOSITE_ROOT);
			Optional<File> compositeRootProject = FileUtils.getAbsoluteFile(dialogSettings.get(SETTINGS_KEY_COMPOSITE_ROOT_PROJECT));
			
			this.configuration.setUseCompositeRoot(useCompositeRoot);
			this.configuration.setRootProject(compositeRootProject.orNull());
		}
		
	    public CompositeRootProjectConfiguration getConfiguration() {
	        return this.configuration;
	    }
	    
}
