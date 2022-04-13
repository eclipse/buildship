package org.eclipse.buildship.model.internal;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import org.eclipse.buildship.model.SourceSet;

public class DefaultSourceSet implements SourceSet, Serializable {

    private final String name;
    private final Set<File> runtimeClasspath;
    private final Set<File> srcDirs;

    public DefaultSourceSet(String name, Set<File> runtimeClasspath, Set<File> srcDirs) {
        this.name = name;
        this.runtimeClasspath = runtimeClasspath;
        this.srcDirs = srcDirs;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Set<File> getRuntimeClasspath() {
        return this.runtimeClasspath;
    }

    @Override
    public Set<File> getSrcDirs() {
        return this.srcDirs;
    }
}
