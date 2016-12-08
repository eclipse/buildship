/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin

import org.gradle.script.lang.kotlin.support.KotlinBuildScriptModel
import org.jetbrains.kotlin.script.KotlinScriptExternalDependencies
import org.jetbrains.kotlin.script.ScriptContents
import org.jetbrains.kotlin.script.ScriptDependenciesResolver
import org.jetbrains.kotlin.script.asFuture
import java.io.File
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.Callable

class GradleKotlinScriptDependenciesResolver : ScriptDependenciesResolver {

	override fun resolve(script: ScriptContents,
				environment: Map<String, Any?>?,
				report: (ScriptDependenciesResolver.ReportSeverity, String, ScriptContents.Position?) -> Unit,
				previousDependencies: KotlinScriptExternalDependencies?) : Future<KotlinScriptExternalDependencies?> {
		if (environment == null) {
			return makeDependencies(emptyList()).asFuture()
		} else {
            return retrieveDependenciesFromProject(environment).asFuture()
		}
    }

	@Suppress("UNCHECKED_CAST")
	private fun retrieveDependenciesFromProject(environment: Map<String, Any?>): KotlinScriptExternalDependencies {
        val rtPath = environment["rtPath"] as List<File>
        val projectRoot = environment["rootProject"] as File
        //val distributionType = environment["distributionType"] as String
        //val distributionConfig = environment["distributionConfig"] as String?
        val gradleUserHome = environment["gradleUserHome"] as File?
        val isOffline = environment["isOffline"] as Boolean

        val classpath = KotlinModelQuery.retrieveKotlinBuildScriptModelFrom(projectRoot, gradleUserHome, isOffline).classPath
        val gradleKotlinJar = classpath.filter { cp -> cp.name.startsWith("gradle-script-kotlin-") }
        val gradleInstallation = classpath.find { it.absolutePath.contains("dists") && it.parentFile.name.equals("lib") }!!.parentFile.parentFile
        val sources = gradleKotlinJar + buildSrcRootsOf(projectRoot) + sourceRootsOf(gradleInstallation)
        return makeDependencies(rtPath + classpath, sources)
    }

    /**
     * Returns all conventional source directories under buildSrc if any.
     * This won't work for buildSrc projects with a custom source directory layout
     * but should account for the majority of cases and it's cheap.
     */
    private fun buildSrcRootsOf(projectRoot: File): Collection<File> =
        subDirsOf(File(projectRoot, "buildSrc/src/main"))

    private fun sourceRootsOf(gradleInstallation: File): Collection<File> =
        subDirsOf(File(gradleInstallation, "src"))

    private fun subDirsOf(dir: File): Collection<File> =
        if (dir.isDirectory)
            dir.listFiles().filter { it.isDirectory }
        else
            emptyList()

    private fun makeDependencies(classPath: Iterable<File>, sources: Iterable<File> = emptyList()): KotlinScriptExternalDependencies =
        object : KotlinScriptExternalDependencies {
            override val classpath = classPath
            override val imports = implicitImports
            override val sources = sources
        }

    companion object {
        val implicitImports = listOf(
            "org.gradle.api.plugins.*",
            "org.gradle.script.lang.kotlin.*")
    }
}
