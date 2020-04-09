/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.generators;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PlatformStatus {

    private static List<String> listFromString(final String value) {
        final List<String> result = new ArrayList<>();
        for (final StringTokenizer tokenizer = new StringTokenizer(value, ","); tokenizer.hasMoreTokens(); result.add(tokenizer
                .nextToken())) {
        }
        return result;
    }

    private final String id;
    private final String name;
    private final String fileName;
    private String       format;
    private List<String>         images;

    private boolean      hasErrors = false;

    PlatformStatus(final Element anElement) {
        super();
        final NamedNodeMap attributes = anElement.getAttributes();
        id = attributes.getNamedItem("id").getNodeValue();
        Node node = attributes.getNamedItem("name");
        name = node == null ? "" : node.getNodeValue();
        fileName = attributes.getNamedItem("fileName").getNodeValue();
        node = attributes.getNamedItem("format");
        if (node != null) {
            format = node.getNodeValue();
        }
        node = attributes.getNamedItem("images");
        if (node != null) {
            images = listFromString(node.getNodeValue());
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getFormat() {
        return format;
    }

    public String getId() {
        return id;
    }

    public List<String> getImages() {
        return images;
    }

    public String getName() {
        return name;
    }

    public boolean hasErrors() {
        return hasErrors;
    }

    public void registerError() {
        hasErrors = true;
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("PlatformStatus(");
        buffer.append("id=" + id);
        buffer.append(", name=" + name);
        buffer.append(", filename=" + fileName);
        buffer.append(")");
        return buffer.toString();
    }
}
