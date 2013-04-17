/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng;

/**
 * A custom Ant task that finds compile logs containing compile errors. The
 * compile logs with errors are sent as email attachments using information in
 * monitor.properties.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class CompileErrorCheck extends Task {

    private static final class CompilerErrorCheckerHandler extends DefaultHandler {

        boolean hasErrors = false;

        public boolean hasErrors() {
            return hasErrors;
        }

        @Override
        public void startElement(final String uri, final String localName, final String name, final Attributes attributes)
                throws SAXException {
            if (hasErrors) {
                return;
            }
            if ("problem_summary".equals(name)) {
                // problem_summary name
                final String value = attributes.getValue("errors");
                hasErrors = (value != null) && !value.equals("0");
            }
        }
    }

    // test
    public static void main(final String[] args) {
        final CompileErrorCheck checker = new CompileErrorCheck();
        checker.install = "d:/compilelogs";
        checker.execute();
    }

    // directory containing of build source, parent of features and plugins
    private String       install = "";

    // keep track of compile logs containing errors
    private final Vector logsWithErrors;

    // keep track of the factory to use
    private SAXParser    parser;

    public CompileErrorCheck() {
        logsWithErrors = new Vector();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        parser = null;

        try {
            parser = factory.newSAXParser();
        }
        catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        catch (final SAXException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void execute() throws BuildException {
        if (parser == null) {
            return;
        }
        findLogs(new File(install));
        // sendNotice();
    }

    private void findLogs(final File aFile) {
        if (!aFile.exists()) {
            return;
        }
        // basis case
        if (aFile.isFile()) {
            final String absolutePath = aFile.getAbsolutePath();
            if (absolutePath.endsWith(".xml")) {
                parse(aFile);
            } else if (absolutePath.endsWith(".jar.bin.log") || absolutePath.endsWith("dot.bin.log")) {
                read(aFile);
            }
        } else {
            // recurse into directories looking for and reading compile logs
            final File files[] = aFile.listFiles();

            if (files != null) {
                for (int i = 0, max = files.length; i < max; i++) {
                    findLogs(files[i]);
                }
            }
        }
    }

    /**
     * Gets the install.
     * 
     * @return Returns a String
     */
    public String getInstall() {
        return install;
    }

    private void parse(final File file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
        }
        catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        final InputSource inputSource = new InputSource(reader);

        final CompilerErrorCheckerHandler compilerErrorCheckerHandler = new CompilerErrorCheckerHandler();
        try {
            parser.parse(inputSource, compilerErrorCheckerHandler);
        }
        catch (final SAXException e) {
            e.printStackTrace();
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        finally {
            // make sure we don't leave any file handle open
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (final IOException e) {
                    // ignore
                }
            }
        }

        if (compilerErrorCheckerHandler.hasErrors()) {
            logsWithErrors.add(new File(file.getParentFile(), file.getName().replaceAll(".xml", ".html")));
            System.out.println(file.getName() + " has compile errors.");
        }
    }

    // private void sendNotice() {
    // //send email notification that there are compile errors in the build
    // //send the logs as attachments
    // Enumeration enumeration = logsWithErrors.elements();
    //
    // if (logsWithErrors.size() > 0) {
    // try{
    //
    // Mailer mailer = new Mailer();
    // String [] logFiles = new String [logsWithErrors.size()];
    //
    // int i=0;
    //
    // while (enumeration.hasMoreElements()) {
    // logFiles[i++]=((File) enumeration.nextElement()).getAbsolutePath();
    // }
    //
    // mailer.sendMultiPartMessage("Compile errors in build",
    // "Compile errors in build.  See attached compile logs.", logFiles);
    // } catch (NoClassDefFoundError e){
    // while (enumeration.hasMoreElements()) {
    // String path=((File) enumeration.nextElement()).getAbsolutePath();
    // String
    // nameWithPlugin=path.substring(path.indexOf("plugins"),path.length());
    // System.out.println("Compile errors detected in "+nameWithPlugin);
    // }
    //
    // System.out.println("Unable to send email notice of compile errors.");
    // System.out.println("The j2ee.jar may not be on the Ant classpath.");
    //
    // }
    //
    // }
    //
    // }

    private void read(final File file) {
        // read the contents of the log file, and return contents as a String
        if (file.length() == 0) {
            return;
        }

        BufferedReader in = null;
        String aLine;

        try {
            in = new BufferedReader(new FileReader(file));
        }
        catch (final FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            while ((aLine = in.readLine()) != null) {
                int statusSummaryIndex = aLine.indexOf("problem (");
                if (statusSummaryIndex == -1) {
                    statusSummaryIndex = aLine.indexOf("problems (");
                }

                if ((statusSummaryIndex != -1) && (aLine.indexOf("error", statusSummaryIndex) != -1)) {
                    logsWithErrors.add(file);
                    System.out.println(file.getName() + " has compile errors.");
                    return;
                }
            }
        }
        catch (final IOException e) {
            e.printStackTrace();
        }
        finally {
            // make sure we don't leave any file handle open
            if (in != null) {
                try {
                    in.close();
                }
                catch (final IOException e) {
                    // ignore
                }
            }
        }
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

}
