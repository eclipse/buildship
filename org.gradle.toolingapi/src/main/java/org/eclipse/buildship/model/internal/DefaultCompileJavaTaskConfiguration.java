package org.eclipse.buildship.model.internal;

import java.io.Serializable;

public class DefaultCompileJavaTaskConfiguration implements Serializable {

    private final String encoding;

    public DefaultCompileJavaTaskConfiguration(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return this.encoding;
    }

}
