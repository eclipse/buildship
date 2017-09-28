/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.text.IDocument;

public interface IGradlePartitions {
    /**
     * The name of the properties file partitioning.
     * Value: {@value}
     */
    String PARTITIONING= "___gs_partitioning";  //$NON-NLS-1$

    public static final String MULTILINE_COMMENT = "__gradle_multiline_comment";
    public static final String GRADLEDOC = "__gradledoc";
    public static final String[] PARTITION_TYPES = { MULTILINE_COMMENT, GRADLEDOC };
    public static final String[] PARTITIONS = { IDocument.DEFAULT_CONTENT_TYPE, GRADLEDOC, MULTILINE_COMMENT };
}
