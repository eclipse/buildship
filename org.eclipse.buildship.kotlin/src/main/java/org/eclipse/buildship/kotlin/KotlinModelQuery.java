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
import java.net.URISyntaxException;

import org.gradle.script.lang.kotlin.support.KotlinBuildScriptModel;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

/**
 * Query {@link KotlinBuildScriptModel}.
 *
 * @author Donat Csikos
 */
public class KotlinModelQuery {

    public static KotlinBuildScriptModel execute(File projectDir, String distributionString, File gradleUserHome, boolean isOffline) {
        ProjectConnection connection = null;
        try {
            GradleConnector connector = GradleConnector.newConnector().forProjectDirectory(projectDir);
            applyGradleDistribution(connector, distributionString);
            connection = connector.connect();
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

    /*
     * TODO (donat) the plugin dependencies are not visible when called from GradleKotlinScriptDependenciesResolver
     * so the GradleDistributionSerializer class is copied here.
     */
    private static void applyGradleDistribution(GradleConnector connector, String distributionString) {
        String localInstallationPrefix = "GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(";
        if (distributionString.startsWith(localInstallationPrefix) && distributionString.endsWith("))")) {
            String localInstallationDir = distributionString.substring(localInstallationPrefix.length(), distributionString.length() - 2);
            connector.useInstallation(new File(localInstallationDir));
            return;
        }

        String remoteDistributionPrefix = "GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(";
        if (distributionString.startsWith(remoteDistributionPrefix) && distributionString.endsWith("))")) {
            String remoteDistributionUri = distributionString.substring(remoteDistributionPrefix.length(), distributionString.length() - 2);
            connector.useDistribution(createURI(remoteDistributionUri));
            return;
        }

        String versionPrefix = "GRADLE_DISTRIBUTION(VERSION(";
        if (distributionString.startsWith(versionPrefix) && distributionString.endsWith("))")) {
            String version = distributionString.substring(versionPrefix.length(), distributionString.length() - 2);
            connector.useGradleVersion(version);
            return;
        }

        String wrapperString = "GRADLE_DISTRIBUTION(WRAPPER)";
        if (distributionString.equals(wrapperString)) {
            connector.useBuildDistribution();
            return;
        }
    }

    private static URI createURI(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
