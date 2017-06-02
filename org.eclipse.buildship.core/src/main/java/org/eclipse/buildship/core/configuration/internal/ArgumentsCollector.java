/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.configuration.internal;

import java.util.List;

import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.util.GradleVersion;

import com.google.common.collect.Lists;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Helper class to collect arguments for build and run configurations.
 *
 * @author Donat Csikos
 */
final class ArgumentsCollector {

    private ArgumentsCollector() {
    }

    /**
     * Collect the final list of arguments that should be to the TAPI operations.
     *
     * @param baseArgs the arguments that must be in the result
     * @param buildScansEnabled whether {@code --scan} (or {@code -Dscan} for older Gradle
     *            distributions) should be added to the result
     * @param offlineMode whether {@code --offline} should be added to the resolt
     * @param buildEnvironment the {@link BuildEnvironment} model to determine the current Gradle
     *            version
     * @return the arguments to pass to the TAPI
     */
    public static List<String> collectArguments(List<String> baseArgs, boolean buildScansEnabled, boolean offlineMode, BuildEnvironment buildEnvironment) {
        List<String> arguments = Lists.newArrayList(baseArgs);
        if (buildScansEnabled) {
            String buildScanArgument = buildScanArgumentFor(buildEnvironment);
            if (!arguments.contains(buildScanArgument)) {
                arguments.add(buildScanArgument);
            }
        }
        if (offlineMode && !arguments.contains("--offline")) {
            arguments.add("--offline");
        }
        arguments.addAll(CorePlugin.invocationCustomizer().getExtraArguments());
        return arguments;
    }

    private static String buildScanArgumentFor(BuildEnvironment environment) {
        GradleVersion currentVersion = GradleVersion.version(environment.getGradle().getGradleVersion());
        GradleVersion supportsDashDashScanVersion = GradleVersion.version("3.5");

        if (supportsDashDashScanVersion.compareTo(currentVersion) <= 0) {
            return "--scan";
        } else {
            return "-Dscan";
        }
    }
}
