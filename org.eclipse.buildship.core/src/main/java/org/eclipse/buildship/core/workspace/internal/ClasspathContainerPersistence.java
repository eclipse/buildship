/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.gradle.api.UncheckedIOException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Stores the current state of the gradle classpath container in the workspace metadata area
 * such that it gets persisted across sessions.
 */
final class ClasspathContainerPersistence {

    private final IJavaProject javaProject;

    private ClasspathContainerPersistence(IJavaProject javaProject) {
        this.javaProject = Preconditions.checkNotNull(javaProject);
    }

    void save(List<IClasspathEntry> entries) {
        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<classpath>\n");
        for (IClasspathEntry entry : entries) {
            content.append(this.javaProject.encodeClasspathEntry(entry));
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

    Optional<List<IClasspathEntry>> load() {
        File stateLocation = getStateLocation();
        if (!stateLocation.exists()) {
            return Optional.absent();
        }

        try {
            Element classpathNode = readClasspathNode(stateLocation);
            List<IClasspathEntry> entries = readEntriesFromClasspathNode(classpathNode);
            return Optional.of(entries);
        } catch (Exception e) {
            CorePlugin.logger().error(String.format("Could not read persisted classpath for project %s.", this.javaProject.getProject().getName()), e);
            return Optional.absent();
        }
    }

    private Element readClasspathNode(File stateLocation) throws IOException, ParserConfigurationException, SAXException {
        byte[] bytes = Files.toByteArray(stateLocation);
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Element classpathNode = parser.parse(new InputSource(new ByteArrayInputStream(bytes))).getDocumentElement();
        if (!classpathNode.getNodeName().equalsIgnoreCase("classpath")) {
            throw new IllegalStateException("Classpath file does not contain a <classpath> element.");
        }
        return classpathNode;
    }

    private List<IClasspathEntry> readEntriesFromClasspathNode(Element classpathNode) throws TransformerException {
        List<IClasspathEntry> entries = Lists.newArrayList();
        NodeList domEntries = classpathNode.getElementsByTagName("classpathentry");
        Transformer identity = TransformerFactory.newInstance().newTransformer();
        identity.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        identity.setOutputProperty(OutputKeys.INDENT, "yes");
        for (int i = 0; i < domEntries.getLength(); i++) {
            Node domEntry = domEntries.item(i);
            if (domEntry.getNodeType() == Node.ELEMENT_NODE) {
                String rawEntry = transformNodeToString(domEntry, identity);
                IClasspathEntry entry = this.javaProject.decodeClasspathEntry(rawEntry);
                if (entry == null) {
                    throw new IllegalStateException(String.format("Could not parse classpath entry %s.", rawEntry));
                } else {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    private String transformNodeToString(Node node, Transformer transformer) throws TransformerException {
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }

    private File getStateLocation() {
        return CorePlugin.getInstance().getStateLocation().append("classpath-persistence").append(this.javaProject.getProject().getName()).toFile();
    }

    static void save(IJavaProject javaProject, List<IClasspathEntry> entries) {
        new ClasspathContainerPersistence(javaProject).save(entries);
    }

    static Optional<List<IClasspathEntry>> load(IJavaProject javaProject) {
        return new ClasspathContainerPersistence(javaProject).load();
    }

}
