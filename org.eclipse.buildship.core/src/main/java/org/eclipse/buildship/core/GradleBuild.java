/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core;

import java.util.function.Function;

import org.gradle.tooling.ProjectConnection;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents a Gradle build.
 *
 * @author Donat Csikos
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface GradleBuild {

    /**
     * Synchronizes the workspace with this Gradle build.
     *
     * <p>
     * The method loads the Gradle build configuration and updates the workspace based on the
     * retrieved information.The algorithm is as follows:
     * <ul>
     *   <li>Synchronize all Gradle projects of the Gradle build with the Eclipse workspace
     *       project counterparts. If there are no projects in the workspace at the location then a
     *       new project is created. Then, based on the workspace project state, the synchronization
     *       is as follows:
     *     <ul>
     *       <li>If the workspace project is closed, the project is left unchanged.</li>
     *       <li>If the workspace project is open, the project configuration (name, source
     *           directories, dependencies, etc.) is updated.</li>
     *     </ul>
     *   </li>
     *   <li>Uncouple all open workspace projects for which there is no corresponding Gradle project
     *       in the Gradle build anymore. This includes removing the Gradle project natures and the
     *       corresponding settings file.</li>
     * </ul>
     *
     * <p>
     * This is a long-running operation which blocks the current thread until completion. Progress
     * and cancellation are provided via the monitor. Also, since the synchronization might modify
     * more than one project, the workspace root scheduling rule is acquired for the current thread
     * internally.
     * <p>
     *
     * The result of the synchronization - let it be a success or a failure - is described by the
     * returned {@link SynchronizationResult} instance.
     *
     * @param monitor the monitor to report progress on, or {@code null} if progress reporting is not desired
     * @return the synchronization result
     */
    SynchronizationResult synchronize(IProgressMonitor monitor);

    /**
     * Executes an action in the Gradle runtime.
     *
     * <p>
     * This method instantiates a new connection to the Tooling API, pre-configures it with IDE
     * services and executes the target action. Clients can use this method to load models and
     * execute tasks, tests and custom build actions without explicitly configuring
     * inputs/outputs/cancellation/etc.
     *
     * <p>
     * The following sections show examples how this API can be used.
     *
     * <p>
     * <i>1. Load models</i>
     *
     * <p>
     * <pre><code>
     *     IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("project-name");
     *     GradleBuild build = GradleCore.getWorkspace().getBuild(project).get();
     *
     *     GradleProject model = build.withConnection(connection -> connection.getModel(GradleProject.class), monitor);
     *     System.out.println(model.getBuildDirectory());
     * </code></pre>
     * You can load the <a href="https://docs.gradle.org/current/javadoc/org/gradle/tooling/ProjectConnection.html#model-java.lang.Class-"> default models</a>.
     * Also, You can load custom models provided by Gradle plugins or even inject your own with an init script:
     * <pre><code>
     *     connection.model(ExtendedEclipseModel.class).withArguments("--init-script", "/path/to/init-script/with/model").get();
     * </code></pre>
     * An example project with custom model loading is available at <a href="https://github.com/eclipse/buildship/tree/master/samples/custom-model">https://github.com/eclipse/buildship/tree/master/samples/custom-model</a>.
     *
     * <p>
     * <i>2. Load the available tasks</i>
     *
     * <p>
     * <pre><code>
     *     GradleBuild build = ...
     *     List<String> tasks = build.withConnection(connection -> {
     *         GradleProject model = connection.getModel(GradleProject.class);
     *         return model.getTasks().stream().map(Task::getPath).collect(Collectors.toList());
     *     }, monitor);
     *
     *     tasks.forEach(task -> System.out.println(task));
     * </code></pre>
     *
     * <p>
     * <i>3. Execute a task</i>
     *
     * <p>
     * <pre><code>
     *     GradleBuild build = ...
     *     build.withConnection(connection -> { connection.newBuild().forTasks("build").run(); return null; }, monitor);
     * </code></pre>
     *
     * <p>
     * <i>4. Execute a test</i>
     *
     * <p>
     * <pre><code>
     *     GradleBuild build = ...
     *     build.withConnection(connection -> { connection.newTestLauncher().withJvmTestClasses("org.example.MyTest").run(); return null; }, monitor);
     * </code></pre>
     *
     * <p>
     *
     * <p>
     * This method does not do exception handling. All exceptions are propagated directly to the client.
     *
     * <p>
     * This is a long-running operation which blocks the current thread until completion. Progress
     * and cancellation are provided via the monitor. Also, the workspace root scheduling rule is
     * acquired for the current thread internally.
     *
     * @param action the action to execute
     * @param monitor the monitor to report progress on, or {@code null} if progress reporting is not desired
     * @return the result of the action
     * @throws Exception when the action fails
     */
    <T> T withConnection(Function<ProjectConnection, ? extends T> action, IProgressMonitor monitor) throws Exception;
}
