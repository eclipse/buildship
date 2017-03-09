package org.eclipse.buildship.core.workspace.internal;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.gradle.tooling.CancellationToken;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.events.OperationType;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;
import com.gradleware.tooling.toolingmodel.repository.TransientRequestAttributes;

abstract class ConnectionAwareLongRunningOperationDelegate<T extends LongRunningOperation> implements LongRunningOperation {

    protected final T delegate;

    ConnectionAwareLongRunningOperationDelegate(T delegate) {
        this.delegate = delegate;
    }

    protected void applyRequestAttributes(FixedRequestAttributes fixedAttributes, TransientRequestAttributes transientAttributes) {
        // TODO (donat) this should be moved to the Fixed/Transient attribute classes

        // fixed attributes
        setJavaHome(fixedAttributes.getJavaHome());
        withArguments(fixedAttributes.getArguments());
        setJvmArguments(fixedAttributes.getJvmArguments());

        // transient attributes
        setStandardOutput(transientAttributes.getStandardOutput());
        setStandardError(transientAttributes.getStandardError());
        setStandardInput(transientAttributes.getStandardInput());
        for (ProgressListener listener : transientAttributes.getProgressListeners()) {
            addProgressListener(listener);
        }
        withCancellationToken(transientAttributes.getCancellationToken());
    }

    // -- delegate methods --

    @Override
    public LongRunningOperation addProgressListener(ProgressListener listener) {
        return this.delegate.addProgressListener(listener);
    }

    @Override
    public LongRunningOperation addProgressListener(org.gradle.tooling.events.ProgressListener listener) {
        return this.delegate.addProgressListener(listener);
    }

    @Override
    public LongRunningOperation addProgressListener(org.gradle.tooling.events.ProgressListener listener, Set<OperationType> types) {
        return this.delegate.addProgressListener(listener, types);
    }

    @Override
    public LongRunningOperation addProgressListener(org.gradle.tooling.events.ProgressListener listener, OperationType... types) {
        return this.delegate.addProgressListener(listener, types);
    }

    @Override
    public LongRunningOperation setColorOutput(boolean colorOutput) {
        return this.delegate.setColorOutput(colorOutput);
    }

    @Override
    public LongRunningOperation setJavaHome(File javaHome) {
        return this.delegate.setJavaHome(javaHome);
    }

    @Override
    public LongRunningOperation setJvmArguments(String... jvmArguments) {
        return this.delegate.setJvmArguments(jvmArguments);
    }

    @Override
    public LongRunningOperation setJvmArguments(Iterable<String> jvmArguments) {
        return this.delegate.setJvmArguments(jvmArguments);
    }

    @Override
    public LongRunningOperation setStandardError(OutputStream error) {
        return this.delegate.setStandardError(error);
    }

    @Override
    public LongRunningOperation setStandardInput(InputStream input) {
        return this.delegate.setStandardInput(input);
    }

    @Override
    public LongRunningOperation setStandardOutput(OutputStream output) {
        return this.delegate.setStandardOutput(output);
    }

    @Override
    public LongRunningOperation withArguments(String... arguments) {
        return this.delegate.withArguments(arguments);
    }

    @Override
    public LongRunningOperation withArguments(Iterable<String> arguments) {
        return this.delegate.withArguments(arguments);
    }

    @Override
    public LongRunningOperation withCancellationToken(CancellationToken token) {
        return this.delegate.withCancellationToken(token);
    }
}
