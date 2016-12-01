/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.workspace.internal;

import java.io.ByteArrayInputStream;
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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.preferences.ProjectPluginStatePreferences;

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
        ProjectPluginStatePreferences preferences = CorePlugin.projectPluginStatePreferenceStore().loadProjectPrefs(this.javaProject.getProject());
        preferences.setValue("classpath", content.toString());
        preferences.flush();
    }

    Optional<List<IClasspathEntry>> load() {
        ProjectPluginStatePreferences preferences = CorePlugin.projectPluginStatePreferenceStore().loadProjectPrefs(this.javaProject.getProject());
        String classpath = preferences.getValue("classpath", null);
        if (classpath == null) {
            return Optional.absent();
        }

        try {
            Element classpathNode = readClasspathNode(classpath);
            List<IClasspathEntry> entries = readEntriesFromClasspathNode(classpathNode);
            return Optional.of(entries);
        } catch (Exception e) {
            CorePlugin.logger().error(String.format("Could not read persisted classpath for project %s.", this.javaProject.getProject().getName()), e);
            return Optional.absent();
        }
    }

    private Element readClasspathNode(String classpath) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Element classpathNode = parser.parse(new InputSource(new ByteArrayInputStream(classpath.getBytes()))).getDocumentElement();
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

    static void save(IJavaProject javaProject, List<IClasspathEntry> entries) {
        new ClasspathContainerPersistence(javaProject).save(entries);
    }

    static Optional<List<IClasspathEntry>> load(IJavaProject javaProject) {
        return new ClasspathContainerPersistence(javaProject).load();
    }

}
