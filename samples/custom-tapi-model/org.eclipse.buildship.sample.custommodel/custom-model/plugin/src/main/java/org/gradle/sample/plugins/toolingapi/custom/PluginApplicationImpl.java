package org.gradle.sample.plugins.toolingapi.custom;

import java.io.File;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class PluginApplicationImpl implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<File, Set<String>> projectLocToPluginClassNames;

    public PluginApplicationImpl(Map<File, Set<String>> pluginClassNames) {
        this.projectLocToPluginClassNames = pluginClassNames;
    }

    public boolean hasPlugin(File projectLocation, String type) {
        Set<String> pluginClassNames = this.projectLocToPluginClassNames.get(projectLocation);
        return pluginClassNames != null ? pluginClassNames.contains(type) : false;
    }
}
