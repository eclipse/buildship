package org.eclipse.buildship.ui.internal.wizard.workspacecomposite;

public class ExternalGradleProject {
	//TODO (kuzniarz) initial implementation needs to be finished
	
	private String projectName;
	private String projectPath;
	
	public ExternalGradleProject(String name, String path) {
		projectName = name;
		projectPath = path;
	}
	
	public String getProjectName() {
		return this.projectName;
	}

	public String getProjectPath() {
		return this.projectPath;
	}
}
