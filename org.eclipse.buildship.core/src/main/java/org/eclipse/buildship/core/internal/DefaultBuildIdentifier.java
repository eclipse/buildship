package org.eclipse.buildship.core.internal;

import java.io.File;

import org.eclipse.buildship.BuildIdentifier;
import org.eclipse.buildship.core.util.gradle.GradleDistribution;

public class DefaultBuildIdentifier implements BuildIdentifier {

	private final File projectDir;
    private final GradleDistribution gradleDistribution;

	public DefaultBuildIdentifier(File projectDir, GradleDistribution gradleDistribution) {
		this.projectDir = projectDir;
        this.gradleDistribution = gradleDistribution;
	}

	public File getProjectDir() {
		return this.projectDir;
	}

    public GradleDistribution getGradleDistribution() {
        return this.gradleDistribution;
    }
}
