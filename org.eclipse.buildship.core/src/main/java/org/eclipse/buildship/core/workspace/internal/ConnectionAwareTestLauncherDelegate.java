package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.ResultHandler;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.test.TestOperationDescriptor;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

class ConnectionAwareTestLauncherDelegate extends ConnectionAwareLongRunningOperationDelegate<TestLauncher> implements TestLauncher {

    private final ProjectConnection connection;

    public ConnectionAwareTestLauncherDelegate(ProjectConnection connection, TestLauncher delegate) {
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
                ConnectionAwareTestLauncherDelegate.this.connection.close();
                }
            }

            @Override
            public void onFailure(GradleConnectionException exception) {
                try {
                    resultHandler.onFailure(exception);
                } finally {
                    ConnectionAwareTestLauncherDelegate.this.connection.close();
                }
            }
        });
    }

    // -- delegate methods --

    static TestLauncher create(FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes) {
        ProjectConnection connection = openConnection(fixedAttributes);
        TestLauncher launcher = connection.newTestLauncher();
        ConnectionAwareTestLauncherDelegate result = new ConnectionAwareTestLauncherDelegate(connection, launcher);
        result.applyRequestAttributes(fixedAttributes, transientAttributes);
        return result;
    }

    @Override
    public TestLauncher addProgressListener(ProgressListener listener) {
        return this.delegate.addProgressListener(listener);
    }

    @Override
    public TestLauncher addProgressListener(org.gradle.tooling.events.ProgressListener listener) {
        return this.delegate.addProgressListener(listener);
    }

    @Override
    public TestLauncher addProgressListener(org.gradle.tooling.events.ProgressListener listener, Set<OperationType> types) {
        return this.delegate.addProgressListener(listener, types);
    }

    @Override
    public TestLauncher addProgressListener(org.gradle.tooling.events.ProgressListener listener, OperationType... types) {
        return this.delegate.addProgressListener(listener, types);
    }

    @Override
    public TestLauncher setColorOutput(boolean colorOutput) {
        return this.delegate.setColorOutput(colorOutput);
    }

    @Override
    public TestLauncher setJavaHome(File javaHome) {
        return this.delegate.setJavaHome(javaHome);
    }

    @Override
    public TestLauncher setJvmArguments(String... jvmArguments) {
        return this.delegate.setJvmArguments(jvmArguments);
    }

    @Override
    public TestLauncher setJvmArguments(Iterable<String> jvmArguments) {
        return this.delegate.setJvmArguments(jvmArguments);
    }

    @Override
    public TestLauncher setStandardError(OutputStream error) {
        return this.delegate.setStandardError(error);
    }

    @Override
    public TestLauncher setStandardInput(InputStream input) {
        return this.delegate.setStandardInput(input);
    }

    @Override
    public TestLauncher setStandardOutput(OutputStream output) {
        return this.delegate.setStandardOutput(output);
    }

    @Override
    public TestLauncher withArguments(String... arguments) {
        return this.delegate.withArguments(arguments);
    }

    @Override
    public TestLauncher withArguments(Iterable<String> arguments) {
        return this.delegate.withArguments(arguments);
    }

    @Override
    public TestLauncher withCancellationToken(CancellationToken token) {
        return this.delegate.withCancellationToken(token);
    }

    @Override
    public TestLauncher withJvmTestClasses(String... classNames) {
        return this.delegate.withJvmTestClasses(classNames);
    }

    @Override
    public TestLauncher withJvmTestClasses(Iterable<String> classNames) {
        return this.delegate.withJvmTestClasses(classNames);
    }

    @Override
    public TestLauncher withJvmTestMethods(String className, String... methodNames) {
        return this.delegate.withJvmTestMethods(className, methodNames);
    }

    @Override
    public TestLauncher withJvmTestMethods(String className, Iterable<String> methodNames) {
        return this.delegate.withJvmTestMethods(className, methodNames);
    }

    @Override
    public TestLauncher withTests(TestOperationDescriptor... descriptors) {
        return this.delegate.withTests(descriptors);
    }

    @Override
    public TestLauncher withTests(Iterable<? extends TestOperationDescriptor> descriptors) {
        return this.delegate.withTests(descriptors);
    }
}
