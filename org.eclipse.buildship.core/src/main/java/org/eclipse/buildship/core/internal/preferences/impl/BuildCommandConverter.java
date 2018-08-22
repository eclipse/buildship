/*
 * Copyright (c) 2017 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.buildship.core.internal.preferences.impl;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.internal.CorePlugin;

/**
 * Transforms build commands to XML format and vica versa.
 */
final class BuildCommandConverter {

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_KEY = "key";
    private static final String TAG_COMMANDS = "commands";
    private static final String TAG_COMMAND = "command";
    private static final String TAG_ARGUMENT = "argument";

    private final IProject project;

    private BuildCommandConverter(IProject project) {
        this.project = Preconditions.checkNotNull(project);
    }

    public String toXml(List<ICommand> commands) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element commandsNode = createNode(document, document, TAG_COMMANDS);

            for (ICommand command : commands) {
                Element commandNode = createNode(document, commandsNode, TAG_COMMAND);
                createAttribute(document, commandNode, ATTRIBUTE_NAME, command.getBuilderName());

                Map<String, String> arguments = command.getArguments();
                if (!arguments.isEmpty()) {

                    for (String key : arguments.keySet()) {
                        String value = arguments.get(key);
                        Element argumentNode = createNode(document, commandNode, TAG_ARGUMENT, value);
                        createAttribute(document, argumentNode, ATTRIBUTE_KEY, key);
                    }
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));

            return writer.toString();
        } catch (Exception e) {
            CorePlugin.logger().warn("Cannot save build commands", e);
            return "";
        }
    }

    private Element createNode(Document document, Node parent, String tagName) {
        return createNode(document, parent, tagName, null);
    }

    private Element createNode(Document document, Node parent, String tagName, String textContent) {
        Element element = document.createElement(tagName);
        element.setTextContent(textContent);
        parent.appendChild(element);
        return element;
    }

    private void createAttribute(Document document, Element parent, String name, String value) {
        Attr attribute = document.createAttribute(name);
        attribute.setValue(value);
        parent.setAttributeNode(attribute);
    }

    private List<ICommand> toEntries(String commands) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(commands.getBytes()));

            List<ICommand> result = Lists.newArrayList();

            NodeList commandNodeList = document.getElementsByTagName(TAG_COMMAND);
            for (int i = 0; i < commandNodeList.getLength(); i++) {
                Element eElement = (Element) commandNodeList.item(i);

                ICommand command = this.project.getDescription().newCommand();
                command.setBuilderName(eElement.getAttribute(ATTRIBUTE_NAME));

                NodeList argumentNodeList = eElement.getElementsByTagName(TAG_ARGUMENT);

                Map<String, String> argumentsMap = Maps.newHashMap();
                for (int j = 0; j < argumentNodeList.getLength(); j++) {
                    Element argumentElement = (Element) argumentNodeList.item(j);
                    argumentsMap.put(argumentElement.getAttribute(ATTRIBUTE_KEY), argumentElement.getTextContent());
                }

                command.setArguments(argumentsMap);
                result.add(command);
            }

            return result;

        } catch (Exception e) {
            CorePlugin.logger().warn("Cannot load build commands", e);
            return Collections.emptyList();
        }
    }

    static String toXml(IProject project, List<ICommand> commands) {
        return new BuildCommandConverter(project).toXml(commands);
    }

    static List<ICommand> toEntries(IProject project, String commands) {
        return new BuildCommandConverter(project).toEntries(commands);
    }
}
