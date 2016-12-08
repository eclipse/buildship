/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin

import org.gradle.script.lang.kotlin.provider.KotlinScriptPluginFactory
import org.gradle.script.lang.kotlin.support.KotlinBuildScriptModel
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import java.io.File
import java.net.URI

object KotlinModelQuery {

	fun retrieveKotlinBuildScriptModelFrom(projectDir: File, gradleUserHome: File?, isOffline: Boolean): KotlinBuildScriptModel {
		return withConnectionFrom(connectorFor(projectDir), gradleUserHome) {
			model(KotlinBuildScriptModel::class.java)
				.setJvmArguments("-D${KotlinScriptPluginFactory.modeSystemPropertyName}=${KotlinScriptPluginFactory.classPathMode}")
                .withArguments(if(isOffline) listOf("--offline") else listOf<String>())
				.get()
		}
	}

	fun connectorFor(projectDir: File): GradleConnector =
			GradleConnector.newConnector().forProjectDirectory(projectDir).useDistribution(URI("https://repo.gradle.org/gradle/dist-snapshots/gradle-script-kotlin-3.3-20161205200654+0000-all.zip"))

	inline fun <T> withConnectionFrom(connector: GradleConnector, gradleUserHome: File?, block: ProjectConnection.() -> T): T =
            connector.useGradleUserHomeDir(gradleUserHome).connect().use(block)

	inline fun <T> ProjectConnection.use(block: (ProjectConnection) -> T): T {
		try {
			return block(this)
		} finally {
			close()
		}
	}
}

