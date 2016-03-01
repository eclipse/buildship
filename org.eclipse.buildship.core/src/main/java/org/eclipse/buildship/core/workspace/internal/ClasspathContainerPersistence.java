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
import javax.xml.parsers.ParserConfigurationException;
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
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.GradlePluginsRuntimeException;

/**
 * Stores the current state of the gradle classpath container in the workspace metadata area,
 * so it is persisted accross sessions.
 *
 * @author Stefan Oehme
 *
 */
class ClasspathContainerPersistence {

    private final IJavaProject javaProject;
    private final Transformer nodeToStringTransformer;

    private ClasspathContainerPersistence(IJavaProject javaProject) {
        this.javaProject = Preconditions.checkNotNull(javaProject);
        try {
            this.nodeToStringTransformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerException e) {
            throw new GradlePluginsRuntimeException(e);
        }
        this.nodeToStringTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        this.nodeToStringTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
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

        try {
            Element classpath = readClasspathNode(stateLocation);
            return Optional.of(readEntriesFromClasspathNode(classpath));
        } catch (Exception e) {
            CorePlugin.logger().warn("Could not read persisted classpath for project " + this.javaProject.getProject().getName(), e);
            return Optional.absent();
        }
    }

    private Element readClasspathNode(File stateLocation) throws IOException, ParserConfigurationException, SAXException {
        byte[] bytes = Files.toByteArray(stateLocation);
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Element classpath = parser.parse(new InputSource(new ByteArrayInputStream(bytes))).getDocumentElement();

        if (!classpath.getNodeName().equalsIgnoreCase("classpath")) {
            throw new IllegalStateException("Classpath file does not contain a <classpath> element");
        }
        return classpath;
    }

    private List<IClasspathEntry> readEntriesFromClasspathNode(Element classpath) throws TransformerException {
        List<IClasspathEntry> entries = Lists.newArrayList();
        NodeList domEntries = classpath.getElementsByTagName("classpathentry");
        for (int i = 0; i < domEntries.getLength(); i++) {
            Node domEntry = domEntries.item(i);
            if (domEntry.getNodeType() == Node.ELEMENT_NODE) {
                String rawEntry = nodeToString(domEntry);
                IClasspathEntry entry = this.javaProject.decodeClasspathEntry(rawEntry);
                if (entry == null) {
                    throw new IllegalStateException("Could not parse classpath entry " + rawEntry);
                } else {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    private String nodeToString(Node node) throws TransformerException {
        StringWriter writer = new StringWriter();
        this.nodeToStringTransformer.transform(new DOMSource(node), new StreamResult(writer));
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
