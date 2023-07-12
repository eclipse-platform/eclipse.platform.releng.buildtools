/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
/**
 * This class finds the version of a plug-in, or fragment listed in a feature
 * and writes <element>=<element>_<version> for each in a properties file. The
 * file produced from this task can be loaded by an Ant script to find files in
 * the binary versions of plugins and fragments.
 */

package org.eclipse.releng.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.Task;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class VersionTrackerTask extends Task {

    private class FeatureHandler extends DefaultHandler {

        // Start Element Event Handler
        @Override
        public void startElement(final String uri, final String local, final String qName, final Attributes atts) {

            final String element = atts.getValue("id");
            // need to parse the plugin.xml or fragment.xml for the correct
            // version value since the 3.0 features may list these as "0.0.0"
            if (qName.equals("plugin")) {
                try {
                    allElements.add(getBuildDirectory() + File.separator + "plugins" + File.separator + element + File.separator
                            + "plugin.xml");
                }
                catch (final Exception e) {
                    e.printStackTrace();

                }
            } else if (qName.equals("fragment")) {
                allElements.add(getBuildDirectory() + File.separator + "plugins" + File.separator + element + File.separator
                        + "fragment.xml");
            }
        }
    }

    private class PluginHandler extends DefaultHandler {

        // Start Element Event Handler
        @Override
        public void startElement(final String uri, final String local, final String qName, final Attributes atts) {

            final String element = atts.getValue("id");
            final String version = atts.getValue("version");
            System.out.println("Examining " + element);

            if (qName.equals("plugin") || qName.equals("fragment")) {
                System.out.println("Found plugin " + element);
                elements.put(element, element + "_" + version);
            }
        }
    }

    // test
    public static void main(final String[] args) {
        final VersionTrackerTask Tracker = new VersionTrackerTask(args[1]);
        Tracker.parse(args[0], Tracker.new FeatureHandler());
        Tracker.parse(Tracker.new PluginHandler());
        Tracker.writeProperties(args[2], true);
    }

    private String    buildDirectory;

    private Hashtable<String, String> elements;

    private SAXParser parser;

    private Vector<String>    allElements;

    // the feature to from which to collect version information
    private String    featurePath;

    // the path to the file in which to write the results
    private String    outputFilePath;

    public VersionTrackerTask() {
    }

    public VersionTrackerTask(final String install) {
        elements = new Hashtable<>();
        allElements = new Vector<>();

        @SuppressWarnings("restriction")
        final SAXParserFactory saxParserFactory = org.eclipse.core.internal.runtime.XmlProcessorFactory.createSAXFactoryWithErrorOnDOCTYPE();
        try {
            parser = saxParserFactory.newSAXParser();
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (final SAXException e) {
            e.printStackTrace();
        }

        // directory containing the source for a given build
        buildDirectory = install;
    }

    @Override
    public void execute() {
        final VersionTrackerTask tracker = new VersionTrackerTask(getBuildDirectory());
        tracker.parse(getFeaturePath(), new FeatureHandler());
        tracker.parse(new PluginHandler());
        tracker.writeProperties(getOutputFilePath(), true);
    }

    /**
     * @return Returns the installDirectory.
     */
    public String getBuildDirectory() {
        return buildDirectory;
    }

    /**
     * @return Returns the featurePath.
     */
    public String getFeaturePath() {
        return featurePath;
    }

    /**
     * @return Returns the outputFilePath.
     */
    public String getOutputFilePath() {
        return outputFilePath;
    }

    private void parse(final DefaultHandler handler) {
        for (int i = 0; i < allElements.size(); i++) {
            parse(allElements.elementAt(i).toString(), handler);
        }
    }

    public void parse(final String xmlFile, final DefaultHandler handler) {
        try {
            parser.parse(xmlFile, handler);
        }
        catch (final SAXException e) {
            System.err.println(e);
        }
        catch (final IOException e) {
            System.err.println(e);
        }
    }

    /**
     * @param installDirectory
     *            The installDirectory to set.
     */
    public void setBuildDirectory(final String buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    /**
     * @param featurePath
     *            The featurePath to set.
     */
    public void setFeaturePath(final String featurePath) {
        this.featurePath = featurePath;
    }

    /**
     * @param outputFilePath
     *            The outputFilePath to set.
     */
    public void setOutputFilePath(final String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }

    public void writeProperties(final String propertiesFile, final boolean append) {
        try (final PrintWriter writer = new PrintWriter(new FileWriter(propertiesFile, append))){
            final Enumeration<String> keys = elements.keys();

            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                writer.println(key + "=" + elements.get(key).toString());
                writer.flush();
            }
        }
        catch (final IOException e) {
            System.out.println("Unable to write to file " + propertiesFile);
        }

    }

}
