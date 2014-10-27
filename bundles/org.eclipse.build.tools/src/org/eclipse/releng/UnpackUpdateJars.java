/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UnpackUpdateJars extends Task {

    class StreamHandler extends Thread {

        InputStream is;

        String      type;

        StreamHandler(final InputStream is, final String type) {
            this.is = is;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                final InputStreamReader isr = new InputStreamReader(is);
                final BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(type + ">" + line);
                }
            }
            catch (final IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void main(final String[] args) {
        // TODO Auto-generated method stub
        final UnpackUpdateJars up = new UnpackUpdateJars();
        up.site = "/home/davidw/builds/workspaces/davidwrepos2/compare/4.4";
        up.output = "/home/davidw/builds/workspaces/davidwrepos2/compare/4.4";
        up.execute();
    }

    /**
     * @param args
     */
    // parent to plugins and features directory which contains update jars
    private String site;

    private String output;

    ArrayList      unpackedPlugins = new ArrayList();

    public UnpackUpdateJars() {
        super();
    }

    // unpack features, then unpack plug-ins which are not set to unpack="false"
    @Override
    public void execute() {
        new File(output).mkdirs();
        new File(output + "/features").mkdirs();
        new File(output + "/plugins").mkdirs();

        // extract features
        final File featureDir = new File(site, "features");
        if (!featureDir.exists()) {
            return;
        }
        final File[] features = featureDir.listFiles();
        for (int i = 0; i < features.length; i++) {
            final File feature = features[i];
            if (feature.getName().endsWith(".jar")) {
                final String fileName = feature.getName();
                final String unpackedFeatureName = fileName.substring(0, fileName.length() - 4);
                final File unPackedFeature = new File(output + "/features/" + unpackedFeatureName);
                unzip(feature, unPackedFeature);
                getUnpackedPluginList(new File(unPackedFeature, "feature.xml"));
            }
        }

        // unpack plug-ins
        for (int i = 0; i < unpackedPlugins.size(); i++) {
            final File unpackedPluginDirName = new File(output + "/plugins/" + (String) unpackedPlugins.get(i));
            final File jardPlugin = new File(site, "plugins/" + (String) unpackedPlugins.get(i) + ".jar");
            if (jardPlugin.exists()) {
                unzip(jardPlugin, unpackedPluginDirName);
            }
        }
    }

    public String getOutput() {
        return output;
    }

    public String getSite() {
        return site;
    }

    private void getUnpackedPluginList(final File featureXml) {
        Document aDocument = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(featureXml));
        }
        catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        final InputSource inputSource = new InputSource(reader);
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;

        try {
            builder = factory.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            aDocument = builder.parse(inputSource);
        }
        catch (final SAXException e) {
            e.printStackTrace();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        // Get feature attributes
        final NodeList nodeList = aDocument.getElementsByTagName("plugin");
        if (nodeList == null) {
            return;
        }

        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node pluginNode = nodeList.item(i);
            final NamedNodeMap aNamedNodeMap = pluginNode.getAttributes();
            final Node idNode = aNamedNodeMap.getNamedItem("id");
            final Node versionNode = aNamedNodeMap.getNamedItem("version");
            final String pluginDirName = idNode.getNodeValue() + "_" + versionNode.getNodeValue();
            final Node unpackNode = aNamedNodeMap.getNamedItem("unpack");
            if (unpackNode == null) {
                if (!unpackedPlugins.contains(pluginDirName)) {
                    unpackedPlugins.add(pluginDirName);
                }
                continue;
            }

            if (unpackNode.getNodeValue().toString().trim().toLowerCase().equals("true")) {
                if (!unpackedPlugins.contains(pluginDirName)) {
                    System.out.println(pluginDirName);
                    unpackedPlugins.add(pluginDirName);
                }
                continue;
            }
            // copy file to new location
            final File jardPlugin = new File(site, "plugins/" + pluginDirName + ".jar");
            if (jardPlugin.exists()) {
                if (!jardPlugin.renameTo(new File(output, "plugins/" + pluginDirName + ".jar"))) {
                    System.out.println("Failed to move " + jardPlugin.getAbsolutePath() + " to " + output + "plugins/"
                            + pluginDirName + ".jar");
                }
            }
        }
    }

    public void setOutput(final String output) {
        this.output = output;
    }

    public void setSite(final String site) {
        this.site = site;
    }

    public void unzip(final File src, final File dest) {
        final Runtime rt = Runtime.getRuntime();
        final String command = "unzip -qo " + src.getPath() + " -d " + dest.getPath();
        System.out.println("[exec] " + command);
        Process proc = null;
        try {
            proc = rt.exec(command);
        }
        catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // pick up error messages
        final StreamHandler errorHandler = new StreamHandler(proc.getErrorStream(), "ERROR");

        // pick up output
        final StreamHandler outputHandler = new StreamHandler(proc.getInputStream(), "OUTPUT");

        // kick them off
        errorHandler.start();
        outputHandler.start();

        // capture return code
        int returnCode = 0;
        try {
            returnCode = proc.waitFor();
        }
        catch (final InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (returnCode != 0) {
            System.out.println("returnCode: " + returnCode);
        }

    }
}
