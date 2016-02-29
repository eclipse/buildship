/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Etienne Studer & Donát Csikós (Gradle Inc.) - initial API and implementation and initial documentation
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.gradle.api.UncheckedIOException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.CorePlugin;

/**
 * Stores the current state of the gradle classpath container in the workspace metadata area,
 * so it is persisted accross sessions.
 *
 * @author Stefan Oehme
 *
 */
class ClasspathContainerPersistence {

    private final IJavaProject javaProject;

    private ClasspathContainerPersistence(IJavaProject javaProject) {
        this.javaProject = javaProject;
    }

    public void save(List<IClasspathEntry> entries) {
        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<classpath>\n");
        for (IClasspathEntry entry : entries) {
            content.append(this.javaProject.encodeClasspathEntry(entry) + "\n");
        }
        content.append("</classpath>\n");
        File stateLocation = getStateLocation();
        try {
            Files.createParentDirs(stateLocation);
            Files.write(content, stateLocation, Charsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Optional<List<IClasspathEntry>> load() {
        File stateLocation = getStateLocation();
        if (!stateLocation.exists()) {
            return Optional.absent();
        }
        Element classpath = readClasspathNode(stateLocation);
        if (classpath == null) {
            return Optional.absent();
        }

        List<IClasspathEntry> entries = readEntriesFromClasspathNode(classpath);
        return Optional.of(entries);
    }

    private Element readClasspathNode(File stateLocation) {
        Element classpath;
        try {
            byte[] bytes = Files.toByteArray(stateLocation);
            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            classpath = parser.parse(new InputSource(new ByteArrayInputStream(bytes))).getDocumentElement();
        } catch (Exception e) {
            return null;
        }

        if (!classpath.getNodeName().equalsIgnoreCase("classpath")) {
            return null;
        }
        return classpath;
    }

    private List<IClasspathEntry> readEntriesFromClasspathNode(Element classpath) {
        List<IClasspathEntry> entries = Lists.newArrayList();
        NodeList rawEntries = classpath.getElementsByTagName("classpathentry");
        for (int i = 0; i < rawEntries.getLength(); i++) {
            Node rawEntry = rawEntries.item(i);
            if (rawEntry.getNodeType() == Node.ELEMENT_NODE) {
                IClasspathEntry entry = this.javaProject.decodeClasspathEntry(nodeToString(rawEntry));
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    private String nodeToString(Node node) {
        StringWriter writer = new StringWriter();
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(node), new StreamResult(writer));
        } catch (TransformerException e) {
            return null;
        }
        return writer.toString();
    }

    private File getStateLocation() {
        return CorePlugin.getInstance().getStateLocation().append("classpath-persistence").append(this.javaProject.getProject().getName()).toFile();
    }

    public static void save(IJavaProject javaProject, List<IClasspathEntry> entries) {
        new ClasspathContainerPersistence(javaProject).save(entries);
    }

    public static Optional<List<IClasspathEntry>> load(IJavaProject javaProject) {
        return new ClasspathContainerPersistence(javaProject).load();
    }
}
