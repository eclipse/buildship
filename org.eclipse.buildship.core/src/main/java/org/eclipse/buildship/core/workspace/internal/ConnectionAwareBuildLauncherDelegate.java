package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.model.Launchable;
import org.gradle.tooling.model.Task;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

final class ConnectionAwareBuildLauncherDelegate extends ConnectionAwareLongRunningOperationDelegate<BuildLauncher> implements BuildLauncher {

    private final ProjectConnection connection;

    ConnectionAwareBuildLauncherDelegate(ProjectConnection connection, BuildLauncher delegate) {
        super(delegate);
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            this.delegate.run();
        } finally {
            this.connection.close();
        }
    }

    @Override
    public void run(final ResultHandler<? super Void> resultHandler) {
        this.delegate.run(new ResultHandler<Void>() {

            @Override
            public void onComplete(Void result) {
                try {
                    resultHandler.onComplete(result);
                } finally {
                    ConnectionAwareBuildLauncherDelegate.this.connection.close();
                }
            }

            @Override
            public void onFailure(GradleConnectionException exception) {
                try {
                    resultHandler.onFailure(exception);
                } finally {
                    ConnectionAwareBuildLauncherDelegate.this.connection.close();
                }
            }
        });
    }

    static BuildLauncher create(ProjectConnection connection, FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes) {
        BuildLauncher launcher = connection.newBuild();
        ConnectionAwareBuildLauncherDelegate result = new ConnectionAwareBuildLauncherDelegate(connection, launcher);
        result.applyRequestAttributes(fixedAttributes, transientAttributes);
        return result;
    }

    // -- delegate methods --

    @Override
    public BuildLauncher addProgressListener(ProgressListener listener) {
        return this.delegate.addProgressListener(listener);
    }

    @Override
    public BuildLauncher addProgressListener(org.gradle.tooling.events.ProgressListener listener) {
        return this.delegate.addProgressListener(listener);
    }

    @Override
    public BuildLauncher addProgressListener(org.gradle.tooling.events.ProgressListener listener, Set<OperationType> types) {
        return this.delegate.addProgressListener(listener, types);
    }

    @Override
    public BuildLauncher addProgressListener(org.gradle.tooling.events.ProgressListener listener, OperationType... types) {
        return this.delegate.addProgressListener(listener, types);
    }

    @Override
    public BuildLauncher setColorOutput(boolean colorOutput) {
        return this.delegate.setColorOutput(colorOutput);
    }

    @Override
    public BuildLauncher setJavaHome(File javaHome) {
        return this.delegate.setJavaHome(javaHome);
    }

    @Override
    public BuildLauncher setJvmArguments(String... jvmArguments) {
        return this.delegate.setJvmArguments(jvmArguments);
    }

    @Override
    public BuildLauncher setJvmArguments(Iterable<String> jvmArguments) {
        return this.delegate.setJvmArguments(jvmArguments);
    }

    @Override
    public BuildLauncher setStandardError(OutputStream error) {
        return this.delegate.setStandardError(error);
    }

    @Override
    public BuildLauncher setStandardInput(InputStream input) {
        return this.delegate.setStandardInput(input);
    }

    @Override
    public BuildLauncher setStandardOutput(OutputStream output) {
        return this.delegate.setStandardOutput(output);
    }

    @Override
    public BuildLauncher withArguments(String... arguments) {
        return this.delegate.withArguments(arguments);
    }

    @Override
    public BuildLauncher withArguments(Iterable<String> arguments) {
        return this.delegate.withArguments(arguments);
    }

    @Override
    public BuildLauncher withCancellationToken(CancellationToken token) {
        return this.delegate.withCancellationToken(token);
    }

    @Override
    public BuildLauncher forLaunchables(Launchable... launchables) {
        return this.delegate.forLaunchables(launchables);
    }

    @Override
    public BuildLauncher forLaunchables(Iterable<? extends Launchable> launchables) {
        return this.delegate.forLaunchables(launchables);
    }

    @Override
    public BuildLauncher forTasks(String... tasks) {
        return this.delegate.forTasks(tasks);
    }

    @Override
    public BuildLauncher forTasks(Task... tasks) {
        return this.delegate.forTasks(tasks);
    }

    @Override
    public BuildLauncher forTasks(Iterable<? extends Task> tasks) {
        return this.delegate.forTasks(tasks);
    }
}
