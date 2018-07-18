package org.eclipse.buildship;

import com.google.common.base.Preconditions;

import org.eclipse.core.runtime.IStatus;

public class GradleException extends Exception {

    private static final long serialVersionUID = 1L;

    private final IStatus status;

    public GradleException(IStatus status) {
        this.status = Preconditions.checkNotNull(status);
    }

    public IStatus getStatus() {
        return this.status;
    }

    @Override
    public String getMessage() {
       return this.status.getMessage();
    }

    @Override
    public Throwable getCause() {
        return this.status.getException();
    }
}
