/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.generators;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @version 1.0
 * @author
 */
public class ErrorTracker {

    public static void main(final String[] args) {

        // For testing only. Should not be invoked

        final ErrorTracker anInstance = new ErrorTracker();
        anInstance.loadFile("C:\\junk\\testManifest.xml");
        final String[] theTypes = anInstance.getTypes();
        for (int i = 0; i < theTypes.length; i++) {
            // System.out.println("Type: " + theTypes[i]);
            final PlatformStatus[] thePlatforms = anInstance.getPlatforms(theTypes[i]);
            for (int j = 0; j < thePlatforms.length; j++) {
                // System.out.println("Out ID: " + thePlatforms[j].getId());
            }
        }
    }

    // List of test logs expected at end of build
    private final Vector    testLogs  = new Vector();
    // Platforms keyed on
    private final Hashtable platforms = new Hashtable();
    private final Hashtable logFiles  = new Hashtable();
    private final Hashtable typesMap  = new Hashtable();

    private final Vector    typesList = new Vector();

    private String convertPathDelimiters(final String path) {
        return new File(path).getPath();
    }

    public PlatformStatus[] getPlatforms(final String type) {
        final Vector platformIDs = (Vector) typesMap.get(type);
        final PlatformStatus[] result = new PlatformStatus[platformIDs.size()];
        for (int i = 0; i < platformIDs.size(); i++) {
            result[i] = (PlatformStatus) platforms.get(platformIDs.elementAt(i));
        }
        return result;
    }

    /**
     * Returns the testLogs.
     * 
     * @return Vector
     */
    public Vector getTestLogs() {
        return testLogs;
    }

    // Answer a string array of the zip type names in the order they appear in
    // the .xml file.
    public String[] getTypes() {
        return (String[]) typesList.toArray(new String[typesList.size()]);
    }

    // Answer an array of PlatformStatus objects for a given type.

    public boolean hasErrors(final String id) {
        return ((PlatformStatus) platforms.get(id)).hasErrors();
    }

    public void loadFile(final String fileName) {
        final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = null;
        try {
            parser = docBuilderFactory.newDocumentBuilder();
        }
        catch (final ParserConfigurationException e1) {
            e1.printStackTrace();
        }
        try {

            final Document document = parser.parse(fileName);
            final NodeList elements = document.getElementsByTagName("platform");
            final int elementCount = elements.getLength();
            for (int i = 0; i < elementCount; i++) {
                final PlatformStatus aPlatform = new PlatformStatus((Element) elements.item(i));
                // System.out.println("ID: " + aPlatform.getId());
                platforms.put(aPlatform.getId(), aPlatform);

                final Node zipType = elements.item(i).getParentNode();
                final String zipTypeName = zipType.getAttributes().getNamedItem("name").getNodeValue();

                Vector aVector = (Vector) typesMap.get(zipTypeName);
                if (aVector == null) {
                    typesList.add(zipTypeName);
                    aVector = new Vector();
                    typesMap.put(zipTypeName, aVector);
                }
                aVector.add(aPlatform.getId());

            }

            final NodeList effectedFiles = document.getElementsByTagName("effectedFile");
            final int effectedFilesCount = effectedFiles.getLength();
            for (int i = 0; i < effectedFilesCount; i++) {
                final Node anEffectedFile = effectedFiles.item(i);
                final Node logFile = anEffectedFile.getParentNode();
                String logFileName = logFile.getAttributes().getNamedItem("name").getNodeValue();
                logFileName = convertPathDelimiters(logFileName);
                final String effectedFileID = anEffectedFile.getAttributes().getNamedItem("id").getNodeValue();
                // System.out.println(logFileName);
                Vector aVector = (Vector) logFiles.get(logFileName);
                if (aVector == null) {
                    aVector = new Vector();
                    logFiles.put(logFileName, aVector);

                }
                final PlatformStatus ps = (PlatformStatus) platforms.get(effectedFileID);
                if (ps != null) {
                    aVector.addElement(ps);
                }
            }

            // store a list of the test logs expected after testing
            final NodeList testLogList = document.getElementsByTagName("logFile");
            final int testLogCount = testLogList.getLength();
            for (int i = 0; i < testLogCount; i++) {

                final Node testLog = testLogList.item(i);
                final String testLogName = testLog.getAttributes().getNamedItem("name").getNodeValue();
                final Node typeNode = testLog.getAttributes().getNamedItem("type");
                String type = "test";
                if (typeNode != null) {
                    type = typeNode.getNodeValue();
                }
                if (testLogName.endsWith(".xml") && type.equals("test")) {
                    testLogs.add(testLogName);
                    // System.out.println(testLogName);
                }

            }

            // // Test this mess.
            // Object[] results = platforms.values().toArray();
            // for (int i=0; i < results.length; i++) {
            // PlatformStatus ap = (PlatformStatus) results[i];
            // System.out.println("ID: " + ap.getId() + " passed: " +
            // ap.getPassed());
            // }
            //
            // Enumeration anEnumeration = logFiles.keys();
            // while (anEnumeration.hasMoreElements()) {
            // String aKey = (String) anEnumeration.nextElement();
            // System.out.println("Whack a key: " + aKey);
            // ((PlatformStatus) logFiles.get(aKey)).setPassed(false);
            // }
            //
            // results = platforms.values().toArray();
            // for (int i=0; i < results.length; i++) {
            // PlatformStatus ap = (PlatformStatus) results[i];
            // System.out.println("ID: " + ap.getId() + " passed: " +
            // ap.getPassed());
            // }

        }
        catch (final IOException e) {
            System.out.println("IOException: " + fileName);
            // e.printStackTrace();

        }
        catch (final SAXException e) {
            System.out.println("SAXException: " + fileName);
            e.printStackTrace();

        }
    }

    public void registerError(final String fileName) {
        // System.out.println("Found an error in: " + fileName);
        if (logFiles.containsKey(fileName)) {
            final Vector aVector = (Vector) logFiles.get(fileName);
            for (int i = 0; i < aVector.size(); i++) {
                ((PlatformStatus) aVector.elementAt(i)).registerError();
            }
        } else {

            // If a log file is not specified explicitly it effects
            // all "platforms" except JDT

            final Enumeration values = platforms.elements();
            while (values.hasMoreElements()) {
                final PlatformStatus aValue = (PlatformStatus) values.nextElement();
                if (!aValue.getId().equals("JA") && !aValue.getId().equals("EW") && !aValue.getId().equals("EA")) {
                    aValue.registerError();
                }
            }
        }
    }

}
