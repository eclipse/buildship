package eclipsebuild

import java.io.File

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory

class ExistingJarBundlePlugin implements Plugin<Project> {

    static final String TASK_NAME_PROCESS_BUNDLE = 'processBundle'
    static final String TASK_NAME_CREATE_P2_REPOSITORY = 'createP2Repository'
    static final String TASK_NAME_COMPRESS_P2_REPOSITORY = 'createCompressedP2Repository'
    static final String TASK_NAME_GENERATE_ECLIPSE_PROJECT = 'generateEclipseProject'

    static final String PLUGIN_CONFIGURATION_NAME = 'plugin'

    static final String BUNDLES_STAGING_FOLDER = 'tmp/bundles'
    static final String P2_REPOSITORY_FOLDER = 'repository'

    static class BundleInfoExtension {
        Project project
        Property<String> bundleName
        Property<String> bundleVersion
        Property<String> qualifier
        Property<String> template
        Property<String> packageFilter
        Closure resources = {}

        BundleInfoExtension(Project project) {
            this.project = project
            bundleName = project.objects.property(String)
            bundleVersion = project.objects.property(String)
            qualifier = project.objects.property(String)
            template = project.objects.property(String)
            packageFilter = project.objects.property(String)
        }

        void resources(Closure resources) {
            this.resources = resources
        }
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(BasePlugin.class)

        configureExtensions(project)
        configureConfigurations(project)

        addGenerateEclipseProjectTask(project)
        addProcessBundlesTask(project)
        addCreateP2RepositoryTask(project)
    }

    private void addGenerateEclipseProjectTask(project) {
        project.tasks.create(TASK_NAME_GENERATE_ECLIPSE_PROJECT, ExistingJarBundleEclipseProjectTask) {
            group = Constants.gradleTaskGroupName
            bundleVersion = project.extensions.bundleInfo.bundleVersion
            qualifier = project.extensions.bundleInfo.qualifier
            pluginConfiguration = project.configurations.getByName(ExistingJarBundlePlugin.PLUGIN_CONFIGURATION_NAME)
        }
    }

    private void addProcessBundlesTask(Project project) {
        project.tasks.create(TASK_NAME_PROCESS_BUNDLE, ProcessOsgiBundleTask) {
            group = Constants.gradleTaskGroupName
            dependsOn project.getConfigurations().getByName(PLUGIN_CONFIGURATION_NAME)

            bundleName = project.extensions.bundleInfo.bundleName
            bundleVersion = project.extensions.bundleInfo.bundleVersion
            qualifier = project.extensions.bundleInfo.qualifier
            template = project.extensions.bundleInfo.template
            packageFilter = project.extensions.bundleInfo.packageFilter
            resources = project.extensions.bundleInfo.resources
            target = new File(project.buildDir, "$BUNDLES_STAGING_FOLDER/plugins")
            pluginConfiguration = project.configurations.getByName(ExistingJarBundlePlugin.PLUGIN_CONFIGURATION_NAME)
        }
    }

    private void addCreateP2RepositoryTask(Project project) {
         def task = project.tasks.create(TASK_NAME_CREATE_P2_REPOSITORY, CreateP2RepositoryTask) {
            group = Constants.gradleTaskGroupName
            dependsOn ":${BuildDefinitionPlugin.TASK_NAME_DOWNLOAD_ECLIPSE_SDK}"
            dependsOn TASK_NAME_PROCESS_BUNDLE

            bundleSourceDir = new File(project.buildDir, BUNDLES_STAGING_FOLDER)
            targetRepositoryDir = new File(project.buildDir, P2_REPOSITORY_FOLDER)
        }
    }

    private void configureExtensions(Project project) {
        project.extensions.create('bundleInfo', BundleInfoExtension, project)
    }

    private void configureConfigurations(Project project) {
        ConfigurationContainer configurations = project.getConfigurations()
        configurations.create(PLUGIN_CONFIGURATION_NAME)
            .setVisible(false)
            .setTransitive(false)
            .setDescription("Classpath for deployable plugin jars, not transitive")
    }
}
