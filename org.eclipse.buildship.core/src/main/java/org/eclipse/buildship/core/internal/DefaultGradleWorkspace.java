package org.eclipse.buildship.core.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.eclipse.buildship.BuildIdentifier;
import org.eclipse.buildship.GradleException;
import org.eclipse.buildship.GradleProjectConfigurator;
import org.eclipse.buildship.GradleWorkspace;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.BuildConfiguration;
import org.eclipse.buildship.core.util.gradle.GradleDistribution;
import org.eclipse.buildship.core.workspace.GradleBuild;
import org.eclipse.buildship.core.workspace.NewProjectHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.gradle.tooling.GradleConnector;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.io.Files;

public class DefaultGradleWorkspace implements GradleWorkspace {

	@Override
	public BuildIdentifier newBuildIdentifier(File projectLocation) {
		return new DefaultBuildIdentifier(projectLocation, GradleDistribution.fromBuild());
	}

	@Override
    public BuildIdentifier newBuildIdentifier(File projectLocation, GradleDistribution gradleDistribution) {
        return new DefaultBuildIdentifier(projectLocation, gradleDistribution);
    }

	@Override
	public void performImport(BuildIdentifier id, IProgressMonitor monitor,
			Class<? extends GradleProjectConfigurator> configuratorClass) throws GradleException {

		DefaultBuildIdentifier identifier = (DefaultBuildIdentifier) id;
		BuildConfiguration buildConfiguration = CorePlugin.configurationManager().createBuildConfiguration(
				identifier.getProjectDir(), false, identifier.getGradleDistribution(), null, false, false, false);

		try {
			storeConfiguratorName(identifier.getProjectDir(), configuratorClass);
		} catch (IOException e) {
		    throw new GradleException(new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, "Failed to save configurator", e));
		}

		GradleBuild gradleBuild = CorePlugin.gradleWorkspaceManager().getGradleBuild(buildConfiguration);
		try {
			gradleBuild.synchronize(NewProjectHandler.IMPORT_AND_MERGE, GradleConnector.newCancellationTokenSource(), monitor);
		} catch (CoreException e) {
			throw new GradleException(e.getStatus());
		}
	}

	static void storeConfiguratorName(File projectDir, Class<? extends GradleProjectConfigurator> configuratorClass) throws IOException {
        File preferencesFile = preferencesFile();

        if (!preferencesFile.exists()) {
            Files.createParentDirs(preferencesFile);
            Files.touch(preferencesFile);
        }

        Properties properties = new Properties();
        properties.put(projectDir.getAbsolutePath(), configuratorClass.getCanonicalName());
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(preferencesFile), Charsets.UTF_8)) {
        	properties.store(writer, "");
        }
    }

    public static Optional<String> loadConfiguratorName(File projectDir) {
        try {
            return readConfigurationaName(projectDir);
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    private static Optional<String> readConfigurationaName(File projectDir) throws IOException {
        File preferencesFile = preferencesFile();
        if (preferencesFile.exists()) {
            try (Reader reader = new InputStreamReader(new FileInputStream(preferencesFile), Charsets.UTF_8)) {
                Properties props = new Properties();
                props.load(reader);
                return Optional.fromNullable(props.getProperty(projectDir.getAbsolutePath()));
            }
        }
        return Optional.absent();
    }


	private static File preferencesFile() {
		return CorePlugin.getInstance().getStateLocation().append("project-configurators").toFile();
	}

}
