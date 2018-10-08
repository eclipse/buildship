package org.gradle.sample.plugins.toolingapi.custom;

import java.io.File;

public interface PluginApplication {
    boolean hasPlugin(File projectLocation, String type);
}
