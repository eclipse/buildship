/*
 * Copyright (c) 2015 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.test.fixtures

/**
 * Helper class to define file structures under a target folder.
 */
abstract class FileStructure {

    private File rootFolder

    /**
     * Creates the file structure under the target root.
     * <p/>
     * Usage example:
     * <pre>
     * File root = ...
     * FileStructure.create(root) {
     *     folder('folder-a')
     *     file('folder-b/emptyfile')
     *     file('folder-b/nonemptyfile', 'fileContent')
     * }
     * </pre>
     *
     */
    static FileStructure create(File rootFolder, Closure closure) {
        FileStructure fileStructure = new FileStructure(rootFolder, closure) {}
        closure.call()
        fileStructure
    }

    FileStructure(File rootFolder, Closure closure) {
        this.rootFolder = rootFolder
        closure.setDelegate(this)
        closure.setResolveStrategy(Closure.DELEGATE_FIRST)
    }

    /**
     * Creates an empty file at the specified location.
     *
     * @param location the relative path of the file location
     */
    void file(String location) {
        file(location, '')
    }

    /**
     * Creates an empty file with a  specified content and location.
     * @param location the relative path of the file location
     * @param content the content of the new file
     */
    void file(String location, String content) {
        def result = new File(rootFolder, location)
        result.parentFile.mkdirs()
        result.text = content
    }

    /**
     * Creates a folder at the target location
     * @param location the target location
     */
    void folder(String location) {
        new File(rootFolder, location).mkdirs()
    }

    /**
     * The root folder where the #file() and #folder() methods place their result.
     *
     * @return the root folder
     */
    File getRoot() {
        rootFolder
    }
}
