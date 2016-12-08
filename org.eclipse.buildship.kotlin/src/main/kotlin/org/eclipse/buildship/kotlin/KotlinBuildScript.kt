/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.kotlin

import org.gradle.api.Project
import org.gradle.api.plugins.ObjectConfigurationAction
import org.gradle.script.lang.kotlin.KotlinScriptHandler
import org.jetbrains.kotlin.script.ScriptTemplateDefinition

@ScriptTemplateDefinition(
    resolver = GradleKotlinScriptDependenciesResolver::class,
    scriptFilePattern = ".*\\.gradle\\.kts")
abstract class KotlinBuildScript(project: Project) : Project by project {
    /**
     * Configures the build script classpath for this project.
     *
     * @see [Project.buildscript]
     */
    @Suppress("unused")
    open fun buildscript(@Suppress("unused_parameter") configuration: KotlinScriptHandler.() -> Unit) = Unit

    inline fun apply(crossinline configuration: ObjectConfigurationAction.() -> Unit) =
        project.apply({ it.configuration() })
}
