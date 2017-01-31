/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin;

import java.io.File;
import java.net.URI;

import org.gradle.script.lang.kotlin.support.KotlinBuildScriptModel;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

/**
 * Queries the KotlinModel.
 *
 * @author Donat Csikos
 */
public class KotlinModelQuery {

    public static KotlinBuildScriptModel execute(File projectDir, File gradleUserHome, boolean isOffline) {
        ProjectConnection connection = null;
        try {
            connection = GradleConnector.newConnector()
                    .forProjectDirectory(projectDir)
                    .useDistribution(new URI("https://repo.gradle.org/gradle/dist-snapshots/gradle-script-kotlin-3.3-20161205200654+0000-all.zip")).connect();
            return connection.model(KotlinBuildScriptModel.class)
                .setJvmArguments("-Dorg.gradle.script.lang.kotlin.provider.mode=classpath") // from KotlinScriptPluginFactory
                .withArguments(isOffline ? "--offline" : "")
                .get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}
