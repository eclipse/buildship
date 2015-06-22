/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz (vogella GmbH) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.gradle.model;

/**
 * This factory creates Gradle model specific objects.
 */
public class GradleModelFactory {

	private GradleModelFactory() {
	}

	public static Dependency createDependeny(final String dependencyString) {
		return new Dependency() {

			@Override
			public String getDependencyString() {
				return dependencyString;
			}
		};
	}

	public static SourceSet createSourceSet(final String name, final String path) {
		return new SourceSet() {

			@Override
			public String getPath() {
				return path;
			}

			@Override
			public String getName() {
				return name;
			}
		};
	}

	public static Plugin createPlugin(final String pluginName) {
		return new Plugin() {

			@Override
			public String getName() {
				return pluginName;
			}
		};
	}
}
