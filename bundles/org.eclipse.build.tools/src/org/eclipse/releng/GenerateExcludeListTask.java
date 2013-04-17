/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class GenerateExcludeListTask extends Task {

    public static void main(final String[] args) {
        final GenerateExcludeListTask parser = new GenerateExcludeListTask();
        parser.setMapFile("c:\\temp\\orbit.map");
        parser.setOutputFile("c:\\temp\\orbit.map.new");
        parser.execute();
    }

    private final ArrayList a = new ArrayList();
    private String          mapFile;

    private String          outputFile;

    public GenerateExcludeListTask() {
        super();
    }

    @Override
    public void execute() throws BuildException {
        readMap();
        writeProp();
    }

    public String getMapFile() {
        return mapFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    private void readMap() {
        try {
            final BufferedReader r = new BufferedReader(new FileReader(mapFile));
            String line;
            while ((line = r.readLine()) != null) {
                final int start = line.indexOf("plugin@") + 7;
                final int end = line.indexOf(",");
                String plugin = "";
                if ((start > 0) && (end > 0)) {
                    plugin = line.substring(start, end);
                }
                String version = "";
                final int startv = line.indexOf("version=") + 8;
                final int endv = line.indexOf(",", startv);
                if ((startv > 0) && (endv > 0)) {
                    version = line.substring(startv, endv);
                }
                if ((version != "") && (plugin != "")) {
                    final String l = plugin + "_" + version + ".jar";
                    a.add(l);
                }
            }
            r.close();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void setMapFile(final String mapFile) {
        this.mapFile = mapFile;
    }

    // for old map file format //

    /*
     * private void readMap() { try { BufferedReader r = new BufferedReader(new
     * FileReader(mapFile)); String line; while ((line = r.readLine()) != null)
     * { int start = line.lastIndexOf('/'); int lastcomma =
     * line.lastIndexOf(','); int end = line.length(); if (lastcomma > start) {
     * end = lastcomma; } int lastzip = line.lastIndexOf(".zip"); if (lastzip >
     * start) { String rstring = line.substring(0, lastzip); line = rstring +
     * ".jar"; } if ((start < end) && (start > 0)) { String substr =
     * line.substring(start + 1, end); a.add(substr); } } r.close(); } catch
     * (IOException e) { e.printStackTrace(); } }
     */

    public void setOutputFile(final String outputFile) {
        this.outputFile = outputFile;
    }

    private void writeProp() {

        try {
            final BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
            for (final Iterator iterator = a.iterator(); iterator.hasNext();) {
                final String s = iterator.next().toString();
                if (iterator.hasNext()) {
                    out.write("plugins/" + s + ",");
                } else {
                    out.write("plugins/" + s);
                }
            }
            out.close();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
