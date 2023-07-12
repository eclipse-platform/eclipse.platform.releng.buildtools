/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * Parses feature.xml, plugin.xml, and fragment.xml files
 * 
 */

package org.eclipse.releng;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.tools.ant.BuildException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ElementParser extends DefaultHandler {

    private SAXParser    parser;
    private final Vector<String> plugins;

    private final Vector<String> features;

    public ElementParser() {
        // Create a Xerces SAX Parser
        try {
            @SuppressWarnings("restriction")
            SAXParser p = org.eclipse.core.internal.runtime.XmlProcessorFactory.createSAXParserWithErrorOnDOCTYPE();
            parser = p;
        }
        catch (final SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        // instantiate vectors that will hold lists of plugins and features read
        // from feature.xml
        plugins = new Vector<>();
        features = new Vector<>();
    }

    public void add(final String element, final Vector<String> v) {
        if (!v.contains(element)) {
            v.add(element);
        }
    }

    public Vector<String> getFeatures() {
        return features;
    }

    public Vector<String> getPlugins() {
        return plugins;
    }

    public void parse(final String xmlFile) {

        // Parse the Document
        try {
            parser.parse(xmlFile, this);
        }
        catch (final SAXException e) {
            System.err.println(e);
        }
        catch (final IOException e) {
            System.err.println(e);

        }
    }

    public void parse(final String install, final String type, final String id) {

        String xmlFile = null;

        if (type.equals("feature")) {
            xmlFile = install + "/features/" + id + "/" + "feature.xml";
        }
        if (type.equals("plugin")) {
            xmlFile = install + "/plugins/" + id + "/" + "plugin.xml";
        }
        if (type.equals("fragment")) {
            xmlFile = install + "/plugins/" + "/" + id + "/" + "fragment.xml";
        }

        if (new File(xmlFile).exists()) {
            parse(xmlFile);
        } else {
            throw new BuildException("The following " + type + " " + id + " did not get fetched.");
        }

    }

    // Start Element Event Handler
    @Override
    public void startElement(final String uri, final String local, final String qName, final Attributes atts) {
        if (local.equals("plugin") || local.equals("fragment")) {
            add(atts.getValue("id"), plugins);
        }
        if (local.equals("feature")) {
            add(atts.getValue("id") + "-feature", features);
        }
    }
}
