package org.eclipse.buildship.core.internal;

import java.util.function.Supplier;

import org.gradle.tooling.BuildAction;
import org.gradle.tooling.BuildActionExecuter;
import org.gradle.tooling.BuildActionExecuter.Builder;
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.CancellationTokenSource;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.exceptions.UnsupportedOperationConfigurationException;
import org.gradle.tooling.model.build.BuildEnvironment;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.buildship.core.internal.configuration.GradleArguments;
import org.eclipse.buildship.core.internal.gradle.GradleProgressAttributes;

final class IdeAttachedProjectConnection implements ProjectConnection {

    private final ProjectConnection delegate;
    private final GradleArguments gradleArguments;
    private final GradleProgressAttributes progressAttributes;

    private IdeAttachedProjectConnection(ProjectConnection connection, GradleArguments gradleArguments, GradleProgressAttributes progressAttributes) {
        this.delegate = connection;
        this.gradleArguments = gradleArguments;
        this.progressAttributes = progressAttributes;
    }

    @Override
    public BuildLauncher newBuild() {
        return configuratOperation(() -> this.delegate.newBuild());
    }

    @Override
    public TestLauncher newTestLauncher() {
        return configuratOperation(() -> this.delegate.newTestLauncher());
    }

    @Override
    public <T> ModelBuilder<T> model(Class<T> modelType) {
        return configuratOperation(() -> this.delegate.model(modelType));
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    @Override
    public <T> BuildActionExecuter<T> action(BuildAction<T> buildAction) {
        throw new UnsupportedOperationConfigurationException("Cannot call ProjectConnection.action(BuildAction) method. Supported methods: [model, newBuild, newTestLauncher]");
    }

    @Override
    public Builder action() {
        throw new UnsupportedOperationConfigurationException("Cannot call ProjectConnection.action() method. Supported methods: [model, newBuild, newTestLauncher]");
    }

    @Override
    public <T> T getModel(Class<T> modelType) throws GradleConnectionException, IllegalStateException {
        throw new UnsupportedOperationConfigurationException("Cannot call ProjectConnection.getModel(Class) method. Supported methods: [model, newBuild, newTestLauncher]");
    }

    @Override
    public <T> void getModel(Class<T> modelType, ResultHandler<? super T> handler) throws IllegalStateException {
        throw new UnsupportedOperationConfigurationException("Cannot call ProjectConnection.getModel(Class, ResultHandler) method. Supported methods: [model, newBuild, newTestLauncher]");
    }

    private <T extends LongRunningOperation> T configuratOperation(Supplier<T> operationSupplier) {
        T operation = operationSupplier.get();
        BuildEnvironment buildEnvironment = this.delegate.getModel(BuildEnvironment.class);
        this.gradleArguments.applyTo(operation, buildEnvironment);
        this.gradleArguments.describe(this.progressAttributes, buildEnvironment);
        this.progressAttributes.applyTo(operation);
        return operation;
    }

    public static ProjectConnection newInstance(CancellationTokenSource tokenSource, GradleArguments gradleArguments, IProgressMonitor monitor) {
        GradleConnector connector = GradleConnector.newConnector();
        gradleArguments.applyTo(connector);
        ProjectConnection connection = connector.connect();

        GradleProgressAttributes progressAttributes = GradleProgressAttributes.builder(tokenSource, monitor)
                .forBackgroundProcess()
                .withFullProgress()
                .build();

        return new IdeAttachedProjectConnection(connection, gradleArguments, progressAttributes);
    }


}
