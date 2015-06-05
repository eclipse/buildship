package org.eclipse.buildship.core.project.internal;


public class SourceSet {

    private String name;

    private String path;

    public SourceSet() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
