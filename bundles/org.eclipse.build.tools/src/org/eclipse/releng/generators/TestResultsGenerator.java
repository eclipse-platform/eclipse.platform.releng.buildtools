/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.generators;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version 1.0
 * @author Dean Roberts
 */
public class TestResultsGenerator extends Task {

    private static final String WARNING_SEVERITY       = "WARNING";
    private static final String ERROR_SEVERITY         = "ERROR";
    private static final String ForbiddenReferenceID   = "ForbiddenReference";
    private static final String DiscouragedReferenceID = "DiscouragedReference";

    private static final int    DEFAULT_READING_SIZE   = 8192;

    static final String         elementName            = "testsuite";
    static final String         testResultsToken       = "%testresults%";
    static final String         compileLogsToken       = "%compilelogs%";
    static final String         accessesLogsToken      = "%accesseslogs%";

    private String extractXmlRelativeFileName(final String rootCanonicalPath, final File xmlFile) {
        if (rootCanonicalPath != null) {
            String xmlFileCanonicalPath = null;
            try {
                xmlFileCanonicalPath = xmlFile.getCanonicalPath();
            }
            catch (final IOException e) {
                logException(e);
            }
            if (xmlFileCanonicalPath != null) {
                // + 1 to remove the '\'
                return xmlFileCanonicalPath.substring(rootCanonicalPath.length() + 1).replace('\\', '/');
            }
        }
        return "";
    }

    private void logException(final Throwable e) {
        log(e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            log(stackTraceElement.toString());
        }
    }

    public static byte[] getFileByteContent(final String fileName) throws IOException {
        InputStream stream = null;
        try {
            final File file = new File(fileName);
            stream = new BufferedInputStream(new FileInputStream(file));
            return getInputStreamAsByteArray(stream, (int) file.length());
        }
        finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (final IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Returns the given input stream's contents as a byte array. If a length is
     * specified (ie. if length != -1), only length bytes are returned.
     * Otherwise all bytes in the stream are returned. Note this doesn't close
     * the stream.
     * 
     * @throws IOException
     *             if a problem occurred reading the stream.
     */
    public static byte[] getInputStreamAsByteArray(final InputStream stream, final int length) throws IOException {
        byte[] contents;
        if (length == -1) {
            contents = new byte[0];
            int contentsLength = 0;
            int amountRead = -1;
            do {
                final int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE); // read
                                                                                                // at
                                                                                                // least
                                                                                                // 8K

                // resize contents if needed
                if ((contentsLength + amountRequested) > contents.length) {
                    System.arraycopy(contents, 0, contents = new byte[contentsLength + amountRequested], 0, contentsLength);
                }

                // read as many bytes as possible
                amountRead = stream.read(contents, contentsLength, amountRequested);

                if (amountRead > 0) {
                    // remember length of contents
                    contentsLength += amountRead;
                }
            }
            while (amountRead != -1);

            // resize contents if necessary
            if (contentsLength < contents.length) {
                System.arraycopy(contents, 0, contents = new byte[contentsLength], 0, contentsLength);
            }
        } else {
            contents = new byte[length];
            int len = 0;
            int readSize = 0;
            while ((readSize != -1) && (len != length)) {
                // See PR 1FMS89U
                // We record first the read size. In this case len is the actual
                // read size.
                len += readSize;
                readSize = stream.read(contents, len, length - len);
            }
        }

        return contents;
    }

    public static void main(final String[] args) {
        final TestResultsGenerator test = new TestResultsGenerator();
        test.setDropTokenList("%equinox%,%framework%,%extrabundles%,%other%,%incubator%,%provisioning%,%launchers%,%osgistarterkits%");
        test.getDropTokensFromList(test.dropTokenList);
        test.setIsBuildTested(false);
        test.setXmlDirectoryName("/home/shared/eclipse/eclipse4I/siteDir/equinox/drops/I20120514-1900/testresults/xml");
        test.setHtmlDirectoryName("/home/shared/eclipse/eclipse4I/siteDir/equinox/drops/I20120514-1900/testresults");
        test.setDropDirectoryName("/home/shared/eclipse/eclipse4I/siteDir/equinox/drops/I20120514-1900");
        test.setTestResultsTemplateFileName("/home/davidw/git/eclipse.platform.releng.eclipsebuilder/equinox/publishingFiles/templateFiles/testResults.php.template");

        test.setDropTemplateFileName("/home/davidw/git/eclipse.platform.releng.eclipsebuilder/equinox/publishingFiles/templateFiles/index.php.template");
        test.setTestResultsHtmlFileName("testResults.php");
        test.setDropHtmlFileName("index.php");

        test.setHrefTestResultsTargetPath("testresults");
        test.setCompileLogsDirectoryName("/home/shared/eclipse/eclipse4I/siteDir/equinox/drops/I20120514-1900/compilelogs/plugins");
        test.setHrefCompileLogsTargetPath("compilelogs");
        test.setTestManifestFileName("/home/davidw/git/eclipse.platform.releng.eclipsebuilder/equinox/publishingFiles/testManifest.xml");
        test.execute();
    }

    public Vector           dropTokens;

    public String           testResultsWithProblems   = "\n";
    public String           testResultsXmlUrls        = "\n";

    private DocumentBuilder parser                    = null;
    public ErrorTracker     anErrorTracker;
    public String           testResultsTemplateString = "";

    public String           dropTemplateString        = "";


    public Vector           platformDropFileName;

    // assume tests ran. If no html files are found, this is set to false
    private boolean         testsRan                  = true;

    // Parameters
    // build runs JUnit automated tests
    private boolean         isBuildTested;

    // buildType, I, N
    public String           buildType;

    // Comma separated list of drop tokens
    public String           dropTokenList;

    // Location of the xml files
    public String           xmlDirectoryName;

    // Location of the html files
    public String           htmlDirectoryName;

    // Location of the resulting index.php file.
    public String           dropDirectoryName;

    // Location and name of the template index.php file.
    public String           testResultsTemplateFileName;

    // Location and name of the template drop index.php file.
    public String           dropTemplateFileName;

    // Name of the generated index php file.
    public String           testResultsHtmlFileName;

    // Name of the generated drop index php file;
    public String           dropHtmlFileName;

    // Arbitrary path used in the index.php page to href the
    // generated .html files.
    public String           hrefTestResultsTargetPath;
    // Arbitrary path used in the index.php page to reference the compileLogs
    public String           hrefCompileLogsTargetPath;
    // Location of compile logs base directory
    public String           compileLogsDirectoryName;
    // Location and name of test manifest file
    public String           testManifestFileName;

    // temporary way to force "missing" list not to be printed (until complete
    // solution found)
    private boolean         doMissingList             = true;

    // Initialize the prefix to a default string
    private String          prefix                    = "default";

    private String          testShortName             = "";

    private int             counter                   = 0;

    // Configuration of test machines.
    // Add or change new configurations here
    // and update titles in testResults.php.template.
    // These are the suffixes used for JUnit's XML output files.
    // On each invocation, all files in results directory are
    // scanned, to see if they end with suffixes, and if so,
    // are processed for summary row. The column order is determined by
    // the order listed here.
    // This suffix is determined, at test time, when the files junit files are
    // generated, by the setting of a variable named "platform" in test.xml
    // and associated property files.

    private String[]        testsConfigDefaults       = { "linux.gtk.x86_64_8.0.xml", "macosx.cocoa.x86_64_7.0.xml",
            "win32.win32.x86_7.0.xml"                };
    private String        testsConfigExpected;
    private String[]        testsConfig;

    private int             missingCount              = 0;

    private int countCompileErrors(final String aString) {
        return extractNumber(aString, "error");
    }

    private int countCompileWarnings(final String aString) {
        return extractNumber(aString, "warning");
    }

    private int countDiscouragedWarnings(final String aString) {
        return extractNumber(aString, "Discouraged access:");
    }

    private int countErrors(final String fileName) {
        int errorCount = 0;

        if (new File(fileName).length() == 0) {
            return -1;
        }

        try {
            final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            parser = docBuilderFactory.newDocumentBuilder();

            final Document document = parser.parse(fileName);
            final NodeList elements = document.getElementsByTagName(elementName);

            final int elementCount = elements.getLength();
            if (elementCount == 0) {
                return -1;
            }
            for (int i = 0; i < elementCount; i++) {
                final Element element = (Element) elements.item(i);
                final NamedNodeMap attributes = element.getAttributes();
                Node aNode = attributes.getNamedItem("errors");
                errorCount = errorCount + Integer.parseInt(aNode.getNodeValue());
                aNode = attributes.getNamedItem("failures");
                errorCount = errorCount + Integer.parseInt(aNode.getNodeValue());

            }

        }
        catch (final IOException e) {
            log("IOException: " + fileName);
            logException(e);
            return 0;
        }
        catch (final SAXException e) {
            log("SAXException: " + fileName);
            logException(e);
            return 0;
        }
        catch (final ParserConfigurationException e) {
            logException(e);
        }
        return errorCount;
    }

    private int countForbiddenWarnings(final String aString) {
        return extractNumber(aString, "Access restriction:");
    }

    @Override
    public void execute() {

        anErrorTracker = new ErrorTracker();
        platformDropFileName = new Vector();
        anErrorTracker.loadFile(testManifestFileName);
        getDropTokensFromList(dropTokenList);
        testResultsTemplateString = readFile(testResultsTemplateFileName);
        dropTemplateString = readFile(dropTemplateFileName);

        log("Begin: Generating test results index page");
        log("Parsing XML files");
        parseXml();
        log("Parsing compile logs");
        parseCompileLogs();
        log("End: Generating test results index page");
        writeTestResultsFile();
        writeDropIndexFile();
    }

    private int extractNumber(final String aString, final String endToken) {
        final int endIndex = aString.lastIndexOf(endToken);
        if (endIndex == -1) {
            return 0;
        }

        int startIndex = endIndex;
        while ((startIndex >= 0) && (aString.charAt(startIndex) != '(') && (aString.charAt(startIndex) != ',')) {
            startIndex--;
        }

        final String count = aString.substring(startIndex + 1, endIndex).trim();
        try {
            return Integer.parseInt(count);
        }
        catch (final NumberFormatException e) {
            return 0;
        }

    }

    private void formatAccessesErrorRow(final String fileName, final int forbiddenAccessesWarningsCount,
            final int discouragedAccessesWarningsCount, final StringBuffer buffer) {

        if ((forbiddenAccessesWarningsCount == 0) && (discouragedAccessesWarningsCount == 0)) {
            return;
        }

        final String hrefCompileLogsTargetPath2 = getHrefCompileLogsTargetPath();
        final int i = fileName.indexOf(hrefCompileLogsTargetPath2);

        final String shortName = fileName.substring(i + hrefCompileLogsTargetPath2.length());

        buffer.append("<tr>\n<td>\n").append("<a href=").append("\"").append(hrefCompileLogsTargetPath2).append(shortName)
                .append("\">").append(shortName).append("</a>").append("</td>\n").append("<td align=\"center\">")
                .append("<a href=").append("\"").append(hrefCompileLogsTargetPath2).append(shortName).append("#FORBIDDEN_WARNINGS")
                .append("\">").append(forbiddenAccessesWarningsCount).append("</a>").append("</td>\n")
                .append("<td align=\"center\">").append("<a href=").append("\"").append(hrefCompileLogsTargetPath2)
                .append(shortName).append("#DISCOURAGED_WARNINGS").append("\">").append(discouragedAccessesWarningsCount)
                .append("</a>").append("</td>\n").append("</tr>\n");
    }

    private void formatCompileErrorRow(final String fileName, final int errorCount, final int warningCount,
            final StringBuffer buffer) {

        if ((errorCount == 0) && (warningCount == 0)) {
            return;
        }

        final String hrefCompileLogsTargetPath2 = getHrefCompileLogsTargetPath();
        final int i = fileName.indexOf(hrefCompileLogsTargetPath2);

        final String shortName = fileName.substring(i + hrefCompileLogsTargetPath2.length());

        buffer.append("<tr>\n<td>\n").append("<a href=").append("\"").append(hrefCompileLogsTargetPath2).append(shortName)
                .append("\">").append(shortName).append("</a>").append("</td>\n").append("<td align=\"center\">")
                .append("<a href=").append("\"").append(hrefCompileLogsTargetPath2).append(shortName).append("#ERRORS")
                .append("\">").append(errorCount).append("</a>").append("</td>\n").append("<td align=\"center\">")
                .append("<a href=").append("\"").append(hrefCompileLogsTargetPath2).append(shortName).append("#OTHER_WARNINGS")
                .append("\">").append(warningCount).append("</a>").append("</td>\n").append("</tr>\n");
    }

    // Specific to the RelEng test results page
    private String formatRowReleng(final String fileName, final int errorCount, final boolean link) {

        // If the file name doesn't end with any of the set test configurations,
        // do nothing
        boolean endsWithConfig = false;
        String[] tconfig = getTestsConfig();
        int card = tconfig.length;
        for (int i = 0; i < card; i++) {
            if (fileName.endsWith(tconfig[i])) {
                endsWithConfig = true;
            }
        }
        if (!endsWithConfig) {
            return "";
        }

        String aString = "";
        if (!link) {
            return "<tr><td>" + fileName + "</td><td align=\"center\">" + "DNF </td></tr>";
        }

        if (fileName.endsWith(".xml")) {

            final int begin = fileName.lastIndexOf(File.separatorChar);

            // Get org.eclipse. out of the component name
            final String shortName = fileName.substring(begin + 13, fileName.indexOf('_'));
            String displayName = shortName;

            // If the short name does not start with this prefix
            if (!shortName.startsWith(prefix)) {
                // If the prefix is not yet set
                if (prefix == "default") {
                    // Set the testShortName variable to the current short name
                    testShortName = shortName;
                    counter = 0;
                    // Set new prefix
                    prefix = shortName.substring(0, shortName.indexOf(".tests") + 6);
                    aString = aString + "\n<tbody>\r<tr><td><b>" + prefix + ".*" + "</b><td><td><td><td>";
                    aString = aString + "<tr><td><p>" + shortName;

                    // Loop until the matching string postfix(test config.) is
                    // found
                    while ((counter < card) && !fileName.endsWith(tconfig[counter])) {
                        aString = aString + "<td align=\"center\">-</td>";
                        counter++;
                    }
                } else {
                    // Set new prefix
                    prefix = shortName.substring(0, shortName.indexOf(".tests") + 6);

                    // Loop until the matching string postfix(test config.) is
                    // found
                    while ((counter < card) && !fileName.endsWith(tconfig[counter])) {
                        aString = aString + "<td align=\"center\">-</td>";
                        counter++;
                    }

                    // In this case, the new prefix should be set with the short
                    // name under it,
                    // since this would mean that the team has more than one
                    // component test
                    if (!shortName.endsWith("tests")) {
                        aString = aString + "\n<tbody>\n<tr><td><b>" + prefix + ".*" + "</b><td><td><td><td>";
                        aString = aString + "<tr><td><p>" + shortName;
                    }
                    // The team has only one component test
                    else {
                        aString = aString + "\n<tbody><tr><td><b>" + shortName;
                    }
                    testShortName = shortName;

                    counter = 0;
                }
            }
            // If the file's short name starts with the current prefix
            if (shortName.startsWith(prefix)) {
                // If the new file has a different short name than the current
                // one
                if (!shortName.equals(testShortName)) {
                    // Fill the remaining cells with '-'. These files will later
                    // be listed as
                    // missing
                    while (counter < card) {
                        aString = aString + "<td align=\"center\">-</td>";
                        counter++;
                    }
                    counter = 0;
                    // Print the component name
                    aString = aString + "<tr><td><p>" + shortName;
                    // Loop until the matching string postfix(test config.) is
                    // found
                    while ((counter < card) && !fileName.endsWith(tconfig[counter])) {
                        aString = aString + "<td align=\"center\">-</td>";
                        counter++;
                    }
                } else {
                    // Loop until the matching string postfix(test config.) is
                    // found
                    while ((counter < card) && !fileName.endsWith(tconfig[counter])) {
                        aString = aString + "<td align=\"center\">-</td>";
                        counter++;
                    }
                    // If the previous component has no more test files left
                    if (counter == card) {
                        counter = 0;
                        // Print the new component name
                        aString = aString + "<tr><td><p>" + shortName;
                        // Loop until the matching string postfix(test config.)
                        // is found
                        while ((counter < card) && !fileName.endsWith(tconfig[counter])) {
                            aString = aString + "<td align=\"center\">-</td>";
                            counter++;
                        }
                    }
                }

                testShortName = shortName;

                if (errorCount != 0) {
                    aString = aString + "<td align=\"center\"><b>";
                } else {
                    aString = aString + "<td align=\"center\">";
                }

                // Print number of errors
                if (errorCount != 0) {
                    displayName = "<font color=\"#ff0000\">" + "(" + String.valueOf(errorCount) + ")" + "</font>";
                } else {
                    displayName = "(0)";
                }

                // Reference
                if (errorCount == -1) {
                    aString = aString.concat(displayName);
                } else {
                    // rawfilename is file name with no extension.
                    String rawfilename=fileName.substring(begin + 1, fileName.length() - 4);
                    aString = aString + "<a href=" + "\"" + hrefTestResultsTargetPath + "/html/"
                            + rawfilename + ".html" + "\">" + displayName + "</a>";
                    aString = aString + "&nbsp;<a style=\"color:#AAAAAA\" title=\"XML Test Result (e.g. for importing into the Eclipse JUnit view)\" href=\"" + hrefTestResultsTargetPath + "/xml/"
                            + rawfilename + ".xml" + "\">(XML)</a>";
                }

                if (errorCount == -1) {
                    aString = aString + "<font color=\"#ff0000\">DNF";
                }

                if (errorCount != 0) {
                    aString = aString + "</font></b></td>";
                } else {
                    aString = aString + "</td>";
                }
                counter++;
            }
        }

        return aString;
    }

    public String getBuildType() {
        return buildType;
    }

    /**
     * Gets the compileLogsDirectoryName.
     * 
     * @return Returns a String
     */
    public String getCompileLogsDirectoryName() {
        return compileLogsDirectoryName;
    }

    public String getDropDirectoryName() {
        return dropDirectoryName;
    }

    /**
     * Gets the dropHtmlFileName.
     * 
     * @return Returns a String
     */
    public String getDropHtmlFileName() {
        return dropHtmlFileName;
    }

    /**
     * Gets the dropTemplateFileName.
     * 
     * @return Returns a String
     */
    public String getDropTemplateFileName() {
        return dropTemplateFileName;
    }

    public String getDropTokenList() {
        return dropTokenList;
    }

    /**
     * @return
     */
    public Vector getDropTokens() {
        return dropTokens;
    }

    protected void getDropTokensFromList(final String list) {
        final StringTokenizer tokenizer = new StringTokenizer(list, ",");
        dropTokens = new Vector();

        while (tokenizer.hasMoreTokens()) {
            dropTokens.add(tokenizer.nextToken());
        }
    }

    /**
     * Gets the hrefCompileLogsTargetPath.
     * 
     * @return Returns a String
     */
    public String getHrefCompileLogsTargetPath() {
        return hrefCompileLogsTargetPath;
    }

    /**
     * Gets the hrefTestResultsTargetPath.
     * 
     * @return Returns a String
     */
    public String getHrefTestResultsTargetPath() {
        return hrefTestResultsTargetPath;
    }

    public String getHtmlDirectoryName() {
        return htmlDirectoryName;
    }

    /**
     * Gets the testManifestFileName.
     * 
     * @return Returns a String
     */
    public String getTestManifestFileName() {
        return testManifestFileName;
    }

    public String getTestResultsHtmlFileName() {
        return testResultsHtmlFileName;
    }

    public String getTestResultsTemplateFileName() {
        return testResultsTemplateFileName;
    }

    /**
     * @return
     */
    public String getTestResultsWithProblems() {
        return testResultsWithProblems;
    }

    /**
     * @return
     */
    public String getTestResultsXmlUrls() {
        return testResultsXmlUrls;
    }

    public String getXmlDirectoryName() {
        return xmlDirectoryName;
    }

    public boolean isBuildTested() {
        return isBuildTested;
    }

    private void parseCompileLog(final String log, final StringBuffer compilerLog, final StringBuffer accessesLog) {
        int errorCount = 0;
        int warningCount = 0;
        int forbiddenWarningCount = 0;
        int discouragedWarningCount = 0;

        final File file = new File(log);
        Document aDocument = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            final InputSource inputSource = new InputSource(reader);
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            aDocument = builder.parse(inputSource);
        }
        catch (final SAXException e) {
            logException(e);
        }
        catch (final IOException e) {
            logException(e);
        }
        catch (final ParserConfigurationException e) {
            logException(e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (final IOException e) {
                    // ignore
                }
            }
        }

        if (aDocument == null) {
            return;
        }
        // Get summary of problems
        final NodeList nodeList = aDocument.getElementsByTagName("problem");
        if ((nodeList == null) || (nodeList.getLength() == 0)) {
            return;
        }

        final int length = nodeList.getLength();
        for (int i = 0; i < length; i++) {
            final Node problemNode = nodeList.item(i);
            final NamedNodeMap aNamedNodeMap = problemNode.getAttributes();
            final Node severityNode = aNamedNodeMap.getNamedItem("severity");
            final Node idNode = aNamedNodeMap.getNamedItem("id");
            if (severityNode != null) {
                final String severityNodeValue = severityNode.getNodeValue();
                if (WARNING_SEVERITY.equals(severityNodeValue)) {
                    // this is a warning
                    // need to check the id
                    final String nodeValue = idNode.getNodeValue();
                    if (ForbiddenReferenceID.equals(nodeValue)) {
                        forbiddenWarningCount++;
                    } else if (DiscouragedReferenceID.equals(nodeValue)) {
                        discouragedWarningCount++;
                    } else {
                        warningCount++;
                    }
                } else if (ERROR_SEVERITY.equals(severityNodeValue)) {
                    // this is an error
                    errorCount++;
                }
            }
        }
        if (errorCount != 0) {
            // use wildcard in place of version number on directory names
            // log(log + "/n");
            String logName = log.substring(getCompileLogsDirectoryName().length() + 1);
            final StringBuffer buffer = new StringBuffer(logName);
            buffer.replace(logName.indexOf("_") + 1, logName.indexOf(File.separator, logName.indexOf("_") + 1), "*");
            logName = new String(buffer);

            anErrorTracker.registerError(logName);
        }
        final String logName = log.replaceAll(".xml", ".html");
        formatCompileErrorRow(logName, errorCount, warningCount, compilerLog);
        formatAccessesErrorRow(logName, forbiddenWarningCount, discouragedWarningCount, accessesLog);
    }

    public void parseCompileLogs() {

        final StringBuffer compilerString = new StringBuffer();
        final StringBuffer accessesString = new StringBuffer();
        processCompileLogsDirectory(compileLogsDirectoryName, compilerString, accessesString);
        if (compilerString.length() == 0) {
            compilerString.append("None");
        }
        if (accessesString.length() == 0) {
            accessesString.append("None");
        }
        testResultsTemplateString = replace(testResultsTemplateString, compileLogsToken, String.valueOf(compilerString));

        testResultsTemplateString = replace(testResultsTemplateString, accessesLogsToken, String.valueOf(accessesString));
    }

    public void parseXml() {

        final File sourceDirectory = new File(xmlDirectoryName);

        if (sourceDirectory.exists()) {

            String replaceString = "";

            final File[] xmlFileNames = sourceDirectory.listFiles();
            Arrays.sort(xmlFileNames);

            File sourceDirectoryParent = sourceDirectory.getParentFile();
            if (sourceDirectoryParent != null) {
                sourceDirectoryParent = sourceDirectoryParent.getParentFile();
            }
            String sourceDirectoryCanonicalPath = null;
            try {
                sourceDirectoryCanonicalPath = sourceDirectoryParent.getCanonicalPath();
            }
            catch (final IOException e) {
                logException(e);
            }
            for (int i = 0; i < xmlFileNames.length; i++) {
                if (xmlFileNames[i].getPath().endsWith(".xml")) {
                    final String fullName = xmlFileNames[i].getPath();
                    final int errorCount = countErrors(fullName);
                    if (errorCount != 0) {
                        final String testName = xmlFileNames[i].getName().substring(0, xmlFileNames[i].getName().length() - 4);
                        testResultsWithProblems = testResultsWithProblems.concat("\n" + testName);
                        testResultsXmlUrls = testResultsXmlUrls.concat("\n"
                                + extractXmlRelativeFileName(sourceDirectoryCanonicalPath, xmlFileNames[i]));
                        anErrorTracker.registerError(fullName.substring(getXmlDirectoryName().length() + 1));
                    }

                    // final String tmp =
                    // ((platformSpecificTemplateList.equals("")) ?
                    // formatRow(xmlFileNames[i].getPath(),
                    // errorCount, true) :
                    // formatRowReleng(xmlFileNames[i].getPath(), errorCount,
                    // true));
                    String tmp = formatRowReleng(xmlFileNames[i].getPath(), errorCount, true);
                    replaceString = replaceString + tmp;

                }
            }
            // check for missing test logs
            replaceString = replaceString + verifyAllTestsRan(xmlDirectoryName);

            testResultsTemplateString = replace(testResultsTemplateString, testResultsToken, replaceString);
            testsRan = true;

        } else {
            testsRan = false;
            log("Test results not found in " + sourceDirectory.getAbsolutePath());
        }

    }

    private void processCompileLogsDirectory(final String directoryName, final StringBuffer compilerLog,
            final StringBuffer accessesLog) {
        final File sourceDirectory = new File(directoryName);
        if (sourceDirectory.isFile()) {
            if (sourceDirectory.getName().endsWith(".log")) {
                readCompileLog(sourceDirectory.getAbsolutePath(), compilerLog, accessesLog);
            }
            if (sourceDirectory.getName().endsWith(".xml")) {
                parseCompileLog(sourceDirectory.getAbsolutePath(), compilerLog, accessesLog);
            }
        }
        if (sourceDirectory.isDirectory()) {
            final File[] logFiles = sourceDirectory.listFiles();
            Arrays.sort(logFiles);
            for (int j = 0; j < logFiles.length; j++) {
                processCompileLogsDirectory(logFiles[j].getAbsolutePath(), compilerLog, accessesLog);
            }
        }
    }

    protected String processDropRow(final PlatformStatus aPlatform) {
        if ("equinox".equalsIgnoreCase(aPlatform.getFormat())) {
            return processEquinoxDropRow(aPlatform);
        }

        String result = "<tr>";
        // result = result + "<td><div align=left>" + getStatusColumn(aPlatform,
        // "", true) + "</div></td>\n";
        result = result + "<td>" + aPlatform.getName() + "</td>";
        result = result + "<td>" + aPlatform.getFileName() + "</td>\n";
        result = result + "</tr>\n";
        return result;
    }

    protected String processDropRows(final PlatformStatus[] platforms) {
        String result = "";
        for (int i = 0; i < platforms.length; i++) {
            result = result + processDropRow(platforms[i]);
        }
        return result;
    }

    /*
     * Generate and return the HTML mark-up for a single row for an Equinox JAR
     * on the downloads page.
     */
    protected String processEquinoxDropRow(final PlatformStatus aPlatform) {
        String result = "<tr>";
        // result = result + "<td align=\"center\">" +
        // getStatusColumn(aPlatform, "/equinox/images/", true) + "</td>\n";
        result = result + "<td>";
        final String filename = aPlatform.getFileName();
        // if there are images, put them in the same table column as the name of
        // the file
        final List images = aPlatform.getImages();
        if ((images != null) && !images.isEmpty()) {
            for (final Iterator iter = images.iterator(); iter.hasNext();) {
                result = result + "<img src=\"" + iter.next() + "\"/>&nbsp;";
            }
        }
        result = result + "<a href=\"download.php?dropFile=" + filename + "\">" + filename + "</a></td>\n";
        result = result + "{$generateDropSize(\"" + filename + "\")}\n";
        result = result + "{$generateChecksumLinks(\"" + filename + "\", $buildlabel)}\n";
        result = result + "</tr>\n";
        return result;
    }

    // Process drop rows specific to each of the platforms
    protected String processPlatformDropRows(final PlatformStatus[] platforms, final String name) {
        String result = "";
        boolean found = false;
        for (int i = 0; i < platforms.length; i++) {
            // If the platform description indicates the platform's name, or
            // "All",
            // call processDropRow
            if (platforms[i].getName().startsWith(name.substring(0, 3)) || platforms[i].getName().equals("All")) {
                result = result + processDropRow(platforms[i]);
                found = true;
                continue;
            }
            // If the platform description indicates "All Other Platforms",
            // process
            // the row locally
            if (platforms[i].getName().equals("All Other Platforms") && !found) {
                if ("equinox".equalsIgnoreCase(platforms[i].getFormat())) {
                    result = processEquinoxDropRow(platforms[i]);
                    continue;
                }
                // final String imageName = getStatusColumn(platforms[i], "",
                // false);
                result = result + "<tr>";
                // result = result + "<td><div align=left>" + imageName +
                // "</div></td>\n";
                result = result + "<td>All " + name + "</td>";
                // generate http, md5 and sha1 links by calling php functions in
                // the template
                result = result + "<td><?php genLinks($_SERVER[\"SERVER_NAME\"],\"${BUILD_ID}\",\"" + platforms[i].getFileName()
                        + "\"); ?></td>\n";
                result = result + "</tr>\n";
            }
        }
        return result;
    }

    private void readCompileLog(final String log, final StringBuffer compilerLog, final StringBuffer accessesLog) {
        final String fileContents = readFile(log);

        final int errorCount = countCompileErrors(fileContents);
        final int warningCount = countCompileWarnings(fileContents);
        final int forbiddenWarningCount = countForbiddenWarnings(fileContents);
        final int discouragedWarningCount = countDiscouragedWarnings(fileContents);
        if (errorCount != 0) {
            // use wildcard in place of version number on directory names
            String logName = log.substring(getCompileLogsDirectoryName().length() + 1);
            final StringBuffer stringBuffer = new StringBuffer(logName);
            stringBuffer.replace(logName.indexOf("_") + 1, logName.indexOf(File.separator, logName.indexOf("_") + 1), "*");
            logName = new String(stringBuffer);

            anErrorTracker.registerError(logName);
        }
        formatCompileErrorRow(log, errorCount, warningCount, compilerLog);
        formatAccessesErrorRow(log, forbiddenWarningCount, discouragedWarningCount, accessesLog);
    }

    public String readFile(final String fileName) {
        byte[] aByteArray = null;
        try {
            aByteArray = getFileByteContent(fileName);
        }
        catch (final IOException e) {
            logException(e);
        }
        if (aByteArray == null) {
            return "";
        }
        return new String(aByteArray);
    }

    private String replace(final String source, final String original, final String replacement) {

        final int replaceIndex = source.indexOf(original);
        if (replaceIndex > -1) {
            String resultString = source.substring(0, replaceIndex);
            resultString = resultString + replacement;
            resultString = resultString + source.substring(replaceIndex + original.length());
            return resultString;
        } else {
            log("Could not find token: " + original);
            return source;
        }

    }

    public void setBuildType(final String buildType) {
        this.buildType = buildType;
    }

    /**
     * Sets the compileLogsDirectoryName.
     * 
     * @param compileLogsDirectoryName
     *            The compileLogsDirectoryName to set
     */
    public void setCompileLogsDirectoryName(final String compileLogsDirectoryName) {
        this.compileLogsDirectoryName = compileLogsDirectoryName;
    }

    public void setDropDirectoryName(final String aString) {
        dropDirectoryName = aString;
    }

    /**
     * Sets the dropHtmlFileName.
     * 
     * @param dropHtmlFileName
     *            The dropHtmlFileName to set
     */
    public void setDropHtmlFileName(final String dropHtmlFileName) {
        this.dropHtmlFileName = dropHtmlFileName;
    }

    /**
     * Sets the dropTemplateFileName.
     * 
     * @param dropTemplateFileName
     *            The dropTemplateFileName to set
     */
    public void setDropTemplateFileName(final String dropTemplateFileName) {
        this.dropTemplateFileName = dropTemplateFileName;
    }

    public void setDropTokenList(final String dropTokenList) {
        this.dropTokenList = dropTokenList;
    }

    /**
     * @param vector
     */
    public void setDropTokens(final Vector vector) {
        dropTokens = vector;
    }

    /**
     * Sets the hrefCompileLogsTargetPath.
     * 
     * @param hrefCompileLogsTargetPath
     *            The hrefCompileLogsTargetPath to set
     */
    public void setHrefCompileLogsTargetPath(final String hrefCompileLogsTargetPath) {
        this.hrefCompileLogsTargetPath = hrefCompileLogsTargetPath;
    }

    /**
     * Sets the hrefTestResultsTargetPath.
     * 
     * @param hrefTestResultsTargetPath
     *            The hrefTestResultsTargetPath to set
     */
    public void setHrefTestResultsTargetPath(final String htmlTargetPath) {
        hrefTestResultsTargetPath = htmlTargetPath;
    }

    public void setHtmlDirectoryName(final String aString) {
        htmlDirectoryName = aString;
    }

    public void setIsBuildTested(final boolean isBuildTested) {
        this.isBuildTested = isBuildTested;
    }

    /**
     * Sets the testManifestFileName.
     * 
     * @param testManifestFileName
     *            The testManifestFileName to set
     */
    public void setTestManifestFileName(final String testManifestFileName) {
        this.testManifestFileName = testManifestFileName;
    }

    public void setTestResultsHtmlFileName(final String aString) {
        testResultsHtmlFileName = aString;
    }

    public void setTestResultsTemplateFileName(final String aString) {
        testResultsTemplateFileName = aString;
    }

    /**
     * @param string
     */
    public void setTestResultsWithProblems(final String string) {
        testResultsWithProblems = string;
    }

    /**
     * @param b
     */
    public void setTestsRan(final boolean b) {
        testsRan = b;
    }

    public void setXmlDirectoryName(final String aString) {
        xmlDirectoryName = aString;
    }

    /**
     * @return
     */
    public boolean testsRan() {
        return testsRan;
    }

    private String verifyAllTestsRan(final String directory) {
        String replaceString = "";
        if (getDoMissingList()) {
            final Enumeration enumeration = (anErrorTracker.getTestLogs()).elements();

            while (enumeration.hasMoreElements()) {
                final String testLogName = enumeration.nextElement().toString();

                if (new File(directory + File.separator + testLogName).exists()) {
                    continue;
                }

                anErrorTracker.registerError(testLogName);
                // final String tmp = ((platformSpecificTemplateList.equals(""))
                // ? formatRow(testLogName, -1, false) : formatRowReleng(
                // testLogName, -1, false));
                String tmp = formatRowReleng(testLogName, -1, false);
                if (missingCount == 0) {
                    replaceString = replaceString
                            + "</table></br>"
                            + "\n"
                            + "<table width=\"65%\" border=\"1\" bgcolor=\"#EEEEEE\" rules=\"groups\" align=\"center\">"
                            + "<tr bgcolor=\"#9999CC\"> <th width=\"80%\" align=\"center\"> Missing Files </th><th  align=\"center\"> Status </th></tr>";
                }
                replaceString = replaceString + tmp;
                testResultsWithProblems = testResultsWithProblems.concat("\n" + testLogName.substring(0, testLogName.length() - 4)
                        + " (file missing)");
                missingCount++;
            }
        } else {
            // Note: we intentionally 
            replaceString = replaceString
                    + "<tbody>\n"
                    + "<tr><td colspan=\"0\"><p><span class=\"footnote\">NOTE: </span>\n"
                    + "Remember that for performance unit test tables, there are never any \"missing files\" listed, if there are any. \n"
                    + "This is expected to be a temporary solution, until an exact fix can be implemented. For more details, see \n"
                    + "<a href=\"https://bugs.eclipse.org/bugs/show_bug.cgi?id=451890\">bug 451890</a>.</p>\n" 
                    + "</td></tr>\n" 
                    + "</tbody>\n";
        }
        return replaceString;
    }

    protected void writeDropIndexFile() {

        final String[] types = anErrorTracker.getTypes();
        for (int i = 0; i < types.length; i++) {
            final PlatformStatus[] platforms = anErrorTracker.getPlatforms(types[i]);
            final String replaceString = processDropRows(platforms);
            dropTemplateString = replace(dropTemplateString, dropTokens.get(i).toString(), replaceString);
        }
        final String outputFileName = dropDirectoryName + File.separator + dropHtmlFileName;
        writeFile(outputFileName, dropTemplateString);
    }

    private void writeFile(final String outputFileName, final String contents) {
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(outputFileName));
            outputStream.write(contents.getBytes());
        }
        catch (final FileNotFoundException e) {
            log("File not found exception writing: " + outputFileName);
        }
        catch (final IOException e) {
            log("IOException writing: " + outputFileName);
        }
        finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (final IOException e) {
                    // ignore
                }
            }
        }
    }

    public void writeTestResultsFile() {
        final String outputFileName = dropDirectoryName + File.separator + testResultsHtmlFileName;
        writeFile(outputFileName, testResultsTemplateString);
    }

    public String getTestsConfigExpected() {
      
        return testsConfigExpected;
    }

    public void setTestsConfigExpected(String testsConfigExpected) {
        this.testsConfigExpected = testsConfigExpected;
    }

    public String[] getTestsConfig() {
        if (testsConfig == null) {
            String expectedConfigParam = getTestsConfigExpected();
            if (expectedConfigParam != null) 
            {
                StringTokenizer tokenizer = new StringTokenizer(expectedConfigParam, " ,\t");
                int nTokens = tokenizer.countTokens();
                String[] tokens = new String[nTokens];
                int i=0;
                while (tokenizer.hasMoreTokens()) {
                    tokens[i++] = tokenizer.nextToken();
                }
                testsConfig = tokens;
            }
            else {
               testsConfig = testsConfigDefaults; 
            }
        }
        return testsConfig;
    }

    public void setTestsConfig(String[] testsConfig) {
        this.testsConfig = testsConfig;
    }

    public boolean getDoMissingList() {
        return doMissingList;
    }

    public void setDoMissingList(boolean doMissingList) {
        this.doMissingList = doMissingList;
    }

}
