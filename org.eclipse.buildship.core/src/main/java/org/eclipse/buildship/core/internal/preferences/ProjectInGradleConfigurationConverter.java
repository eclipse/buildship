/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.preferences;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.gson.Gson;

import org.eclipse.buildship.model.ProjectInGradleConfiguration;
import org.eclipse.buildship.model.SourceSet;
import org.eclipse.buildship.model.TestTask;

/**
 * Transforms classpath entries to XML format and vica versa.
 */
public final class ProjectInGradleConfigurationConverter {

    public static ProjectInGradleConfiguration fromJson(String json) {
        System.out.println(new Gson().fromJson(json, CachedProjectInGradleConfiguration.class));
        return new Gson().fromJson(json, CachedProjectInGradleConfiguration.class);
    }

    public static String toJson(ProjectInGradleConfiguration model) {
        System.out.println(new Gson().toJson(CachedProjectInGradleConfiguration.from(model)));
        return new Gson().toJson(CachedProjectInGradleConfiguration.from(model));
//        // name
//        JsonObject json = new JsonObject();
//        json.addProperty("location", model.getLocation().getAbsolutePath());
//
//        // sourceSets
//        JsonArray jsonSourceSets = new JsonArray();
//        for (SourceSet sourceSet : model.getSourceSets()) {
//            JsonObject jsonSourceSet = new JsonObject();
//            jsonSourceSet.addProperty("name", sourceSet.getName());
//
//            JsonArray jsonRuntimClasspath = new JsonArray();
//            for (File f : sourceSet.getRuntimeClasspath()) {
//                jsonRuntimClasspath.add(f.getAbsolutePath());
//            }
//            jsonSourceSet.add("runtimeClasspath", jsonRuntimClasspath);
//
//            JsonArray jsonSrcDirs = new JsonArray();
//            for (File f : sourceSet.getSrcDirs()) {
//                jsonSrcDirs.add(f.getAbsolutePath());
//            }
//            jsonSourceSet.add("jsonSrcDirs", jsonSrcDirs);
//
//            jsonSourceSets.add(jsonSourceSet);
//        }
//
//        // test tasks
//        JsonArray jsonTestTasks = new JsonArray();
//        for (TestTask testTask : model.getTestTasks()) {
//            JsonObject jsonTestTask = new JsonObject();
//            jsonTestTask.addProperty("path", testTask.getPath());
//
//            JsonArray jsonTestClassesDirs = new JsonArray();
//            for (File f : testTask.getTestClassesDirs()) {
//                jsonTestClassesDirs.add(f.getAbsolutePath());
//            }
//
//            jsonTestTask.add("testClassesDirs", jsonTestClassesDirs);
//            jsonTestTasks.add(jsonTestTask);
//        }
//
//        json.add("sourceSets", jsonSourceSets);
//        json.add("testTasks", jsonTestTasks);
//
//        return new Gson().toJson(json);
    }


    public static class CachedProjectInGradleConfiguration implements ProjectInGradleConfiguration {

        private File location;
        private Set<CachedSourceSet> sourceSets;
        private Set<CachedTestTask> testTasks;

        @Override
        public File getLocation() {
            return this.location;
        }

        public void setLocation(File location) {
            this.location = location;
        }

        @Override
        public Set<CachedSourceSet> getSourceSets() {
            return this.sourceSets;
        }

        public void setSourceSets(Set<CachedSourceSet> sourceSets) {
            this.sourceSets = sourceSets;
        }

        @Override
        public Set<CachedTestTask> getTestTasks() {
            return this.testTasks;
        }

        public void setTestTasks(Set<CachedTestTask> testTasks) {
            this.testTasks = testTasks;
        }

        public static CachedProjectInGradleConfiguration from(ProjectInGradleConfiguration model) {
            CachedProjectInGradleConfiguration result = new CachedProjectInGradleConfiguration();
            result.setLocation(model.getLocation());
            result.setSourceSets(CachedSourceSet.from(model.getSourceSets()));
            result.setTestTasks(CachedTestTask.from(model.getTestTasks()));
            return result;
        }
    }

    public static class CachedSourceSet implements SourceSet {

        private String name;
        private Set<File> runtimeClasspath;
        private Set<File> srcDirs;

        @Override
        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public Set<File> getRuntimeClasspath() {
            return this.runtimeClasspath;
        }

        public void setRuntimeClasspath(Set<File> runtimeClasspath) {
            this.runtimeClasspath = runtimeClasspath;
        }

        @Override
        public Set<File> getSrcDirs() {
            return this.srcDirs;
        }

        public void setSrcDirs(Set<File> srcDirs) {
            this.srcDirs = srcDirs;
        }

        public static Set<CachedSourceSet> from(Set<? extends SourceSet> ss) {
            Set<CachedSourceSet> result = new LinkedHashSet<>();
            for (SourceSet sourceSet : ss) {
                CachedSourceSet css = new CachedSourceSet();
                css.setName(sourceSet.getName());
                css.setRuntimeClasspath(sourceSet.getRuntimeClasspath());
                css.setSrcDirs(sourceSet.getSrcDirs());
                result.add(css);
            }
            return result;
        }
    }

    public static class CachedTestTask implements TestTask {

        private String path;
        private Set<File> testClassesDirs;

        @Override
        public String getPath() {
            return this.path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        @Override
        public Set<File> getTestClassesDirs() {
            return this.testClassesDirs;
        }

        public void setTestClassesDirs(Set<File> testClassesDirs) {
            this.testClassesDirs = testClassesDirs;
        }

        public static Set<CachedTestTask> from(Set<? extends TestTask> testTasks) {
            Set<CachedTestTask> result = new LinkedHashSet<>();
            for (TestTask testTask : testTasks) {
                CachedTestTask ctt = new CachedTestTask();
                ctt.setPath(testTask.getPath());
                ctt.setTestClassesDirs(ctt.getTestClassesDirs());
                result.add(ctt);
            }
            return result;
        }
    }
}
