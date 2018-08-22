/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.preferences.impl;

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

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Transforms classpath entries to XML format and vica versa.
 */
final class ClasspathConverter {

    private final IJavaProject javaProject;

    private ClasspathConverter(IJavaProject javaProject) {
        this.javaProject = Preconditions.checkNotNull(javaProject);
    }

    public String toXml(List<IClasspathEntry> classpath) {
        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<classpath>\n");
        for (IClasspathEntry entry : classpath) {
            content.append(this.javaProject.encodeClasspathEntry(entry));
        }
        content.append("</classpath>\n");
        return content.toString();
    }

    public List<IClasspathEntry> toEntries(String classpath) {
        try {
            Element classpathNode = readClasspathNode(classpath);
            return readEntriesFromClasspathNode(classpathNode);
        } catch (Exception e) {
            CorePlugin.logger().error(String.format("Could not read persisted classpath for project %s.", this.javaProject.getProject().getName()), e);
            return null;
        }
    }

    private Element readClasspathNode(String classpath) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Element classpathNode = parser.parse(new InputSource(new ByteArrayInputStream(classpath.getBytes(Charsets.UTF_8)))).getDocumentElement();
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

    static String toXml(IJavaProject javaProject, List<IClasspathEntry> classpath) {
        return new ClasspathConverter(javaProject).toXml(classpath);
    }

    static List<IClasspathEntry> toEntries(IJavaProject javaProject, String classpath) {
        return new ClasspathConverter(javaProject).toEntries(classpath);
    }
}
