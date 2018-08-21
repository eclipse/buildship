/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.text.IDocument;

import org.eclipse.buildship.ui.UiPlugin;

/**
 * @author Christophe Moine
 */
public interface GradleEditorConstants {

    /**
     * The partitioning for Gradle build script files.
     */
    public static final String PARTITIONING = UiPlugin.PLUGIN_ID + ".buildscript.partitioning";

    /**
     * Token type ID for '{@code /*}'-style multi-line comments.
     */
    public static final String TOKEN_TYPE_MULTILINE_COMMENT = UiPlugin.PLUGIN_ID + ".buildscript.multilinecomment";

    /**
     * Token type ID for '{@code /**}'-style multi-line comments.
     */
    public static final String TOKEN_TYPE_JAVADOC = UiPlugin.PLUGIN_ID + ".buildscript.javadoccomment";

    /**
     * All partitions types defined for Gradle build scripts.
     */
    public static final String[] PARTITIONS = { IDocument.DEFAULT_CONTENT_TYPE, TOKEN_TYPE_JAVADOC, TOKEN_TYPE_MULTILINE_COMMENT };
}
