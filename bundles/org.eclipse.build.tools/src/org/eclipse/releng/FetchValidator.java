/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng;

import java.io.File;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Uses an ElementParser to read a list of feature.xml files and to determine if
 * all plugins defined in the features are present.
 */

public class FetchValidator extends Task {

    // test
    public static void main(final String args[]) {
        final FetchValidator validator = new FetchValidator();
        validator.install = "l:/vabase/team/sonia";
        validator.list = "org.eclipse.platform-feature,org.eclipse.platform.win32-feature,org.eclipse.platform.linux.motif-feature";
        validator.getListOfFeatures(validator.list);

        validator.execute();
    }

    // directory containing of build source, parent of features and plugins
    private String       install = "";

    // Comma separated list of features to parse
    private String       list;
    // Elements to check for post fetch (use name of project in dev.eclipse.org)
    private Vector<String>       features;

    private Vector<String>       plugins;
    // keeps track of missing elements
    private final Vector<String> missingPlugins;

    private final Vector<String> missingFeatures;

    // parser
    ElementParser        parser;

    public FetchValidator() {
        parser = new ElementParser();
        missingPlugins = new Vector<>();
        missingFeatures = new Vector<>();
    }

    private boolean allPresent() {
        // verify presence of all source projects for the build.
        // collect a list of missing plugins (or fragments), and features

        boolean allPresent = true;
        Enumeration<String> enumeration = plugins.elements();

        while (enumeration.hasMoreElements()) {
            final String plugin = enumeration.nextElement();
            if (new File(install + "/plugins/" + plugin).exists()) {
                continue;
            } else {
                missingPlugins.add(plugin);
                allPresent = false;
            }
        }

        enumeration = features.elements();

        while (enumeration.hasMoreElements()) {
            final String feature = enumeration.nextElement();
            if (new File(install + "/features/" + feature).exists()) {
                continue;
            } else {
                missingFeatures.add(feature);
                allPresent = false;
            }
        }

        return allPresent;
    }

    // entry point
    @Override
    public void execute() throws BuildException {
        getListOfFeatures(list);

        if (!allPresent()) {
            String missingFeaturesDesc = "";
            String missingPluginsDesc = "";

            if (missingFeatures.size() > 0) {
                for (int i = 0; i < missingFeatures.size(); i++) {
                    missingFeaturesDesc += "\n\r" + missingFeatures.get(i).toString();
                }
            }

            if (missingPlugins.size() > 0) {
                for (int i = 0; i < missingPlugins.size(); i++) {
                    missingPluginsDesc += "\n\t" + missingPlugins.get(i).toString();
                }
            }
            throw new BuildException(
                    "The following projects did not get fetched: \n"
                            + missingFeaturesDesc
                            + missingPluginsDesc
                            + "\n"
                            + "\n\nPossible causes of missing source files include an incorrect Tag entry in a .map file or problems with CVS repositories.");
        }

        System.out.println("Fetch Complete.");
    }

    /**
     * Gets the install.
     * 
     * @return Returns a String
     */
    public String getInstall() {
        return install;
    }

    /**
     * Gets the list.
     * 
     * @return Returns a String
     */
    public String getList() {
        return list;
    }

    private void getListOfFeatures(final String list) {

        final StringTokenizer tokenizer = new StringTokenizer(list, ",");

        while (tokenizer.hasMoreTokens()) {
            parser.parse(install, "feature", tokenizer.nextToken().trim());
        }

        features = parser.getFeatures();
        plugins = parser.getPlugins();
    }

    /**
     * Sets the install.
     * 
     * @param install
     *            The install to set
     */
    public void setInstall(final String install) {
        this.install = install;
    }

    /**
     * Sets the list.
     * 
     * @param list
     *            The list to set
     */
    public void setList(final String list) {
        this.list = list;
    }

}
