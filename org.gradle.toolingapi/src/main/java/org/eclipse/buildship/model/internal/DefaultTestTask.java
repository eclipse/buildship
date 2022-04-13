package org.eclipse.buildship.model.internal;

import java.io.File;
import java.io.Serializable;
import java.util.Set;

import org.eclipse.buildship.model.TestTask;

public class DefaultTestTask implements TestTask, Serializable {

    private final String path;
    private final Set<File> testClassesDirs;

    public DefaultTestTask(String path, Set<File> testClassesDirs) {
        this.path = path;
        this.testClassesDirs = testClassesDirs;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public Set<File> getTestClassesDirs() {
        return this.testClassesDirs;
    }

}
