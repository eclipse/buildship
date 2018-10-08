package org.gradle.sample.plugins.toolingapi.custom;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.tooling.provider.model.ToolingModelBuilder;
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry;

public class ToolingApiCustomModelPlugin implements Plugin<Project> {
    private final ToolingModelBuilderRegistry registry;

    @Inject
    public ToolingApiCustomModelPlugin(ToolingModelBuilderRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void apply(Project project) {
        this.registry.register(new CustomToolingModelBuilder());
    }

    private static class CustomToolingModelBuilder implements ToolingModelBuilder {
        @Override
        public boolean canBuild(String modelName) {
            return modelName.equals(PluginApplication.class.getName());
        }

        @Override
        public Object buildAll(String modelName, Project rootProject) {
            Set<Project> allProject = rootProject.getAllprojects();
            Map<File, Set<String>> projectLocToPluginClassNames = new HashMap<>();
            for (Project project : allProject) {
                projectLocToPluginClassNames.put(project.getProjectDir(),
                        project.getPlugins().stream().map(plugin -> plugin.getClass().getName()).collect(Collectors.toSet()));
            }
            return new PluginApplicationImpl(projectLocToPluginClassNames);
        }
    }
}
