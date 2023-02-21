/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others. All rights reserved.
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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.releng.generators.TestResultsGenerator.ResultsTable.Cell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @version 1.0
 * @author Dean Roberts (circa 2000!) and David Williams (circa 2016)
 */
public class TestResultsGenerator extends Task {

    public class ResultsTable implements Iterable<String> {

        private Map<String, Row> rows    = new TreeMap<>();
        private List<String>     columns = new ArrayList<>();

        public ResultsTable(ArrayList<String> columns) {
            this.columns = columns;
        }

        public class Cell {

            private Integer errorCount  = null;
            private File    resultsFile = null;

            public Cell(int errorCount, File resultsFile) {
                this.errorCount = errorCount;
                this.resultsFile = resultsFile;
            }

            public Integer getErrorCount() {
                return errorCount;
            }

            public File getResultsFile() {
                return resultsFile;
            }
        }

        private class Row {

            Map<String, Cell> row = new TreeMap<>();

            public Row(List<String> columns) {
                for (String column : columns) {
                    row.put(column, null);
                }
            }

            public Cell getCell(String column) {
                Cell cell = row.get(column);
                return cell;
            }

            public void putCell(String columnName, Integer cellValue, File file) {
                row.put(columnName, new Cell(cellValue, file));
            }
        }

        private Row getRow(String rowname) {
            Row row = rows.get(rowname);
            if (row == null) {
                row = new Row(columns);
                rows.put(rowname, row);
            }
            return row;
        }

        public Cell getCell(String rowName, String columnName) {
            Cell result = getRow(rowName).getCell(columnName);
            return result;
        }

        public int getCellErrorCount(String rowName, String columnName) {
            int result = -1;
            Cell cell = getRow(rowName).getCell(columnName);
            result = cell.getErrorCount();
            return result;
        }

        public File getCellResultsFile(String rowName, String columnName) {
            File result = null;
            Cell cell = getRow(rowName).getCell(columnName);
            result = cell.getResultsFile();
            return result;
        }

        public void putCell(String rowName, String columnName, Integer cellValue, File file) {
            getRow(rowName).putCell(columnName, cellValue, file);
        }

        List<String> getColumns() {
            return columns;
        }

        public void setColumns(List<String> columns) {
            if (this.columns != null) {
                throw new RuntimeException("The columns for the table were already defined");
            }
            this.columns = columns;
        }

        @Override
        public Iterator<String> iterator() {
            return rows.keySet().iterator();
        }
    }

    private static final String HTML_EXTENSION                         = ".html";
    private static final String XML_EXTENSION                          = ".xml";
    private static final String WARNING_SEVERITY                       = "WARNING";
    private static final String ERROR_SEVERITY                         = "ERROR";
    private static final String INFO_SEVERITY                          = "INFO";
    private static final String ForbiddenReferenceID                   = "ForbiddenReference";
    private static final String DiscouragedReferenceID                 = "DiscouragedReference";

    private static final int    DEFAULT_READING_SIZE                   = 8192;

    private static final String elementName                            = "testsuite";

    private ArrayList<String>   expectedConfigs                        = null;
    private static final String EOL                                    = System.lineSeparator();
    private static boolean      DEBUG                                  = false;
    private static String       FOUND_TEST_CONFIGS_FILENAME_DEFAULT    = "testConfigsFound.php";

    private static String       EXPECTED_TEST_CONFIGS_FILENAME_DEFAULT = "testConfigs.php";
    private String              expected_config_type                   = "expected";
    private String              expectedConfigFilename;
    private String              foundConfigFilename;
    private Vector<String>              dropTokens;

    private String              testResultsWithProblems                = EOL;
    private String              testResultsXmlUrls                     = EOL;

    private DocumentBuilder     parser                                 = null;
    private ErrorTracker        anErrorTracker;

    private String              dropTemplateString                     = "";

    // Parameters
    // build runs JUnit automated tests
    private boolean             isBuildTested;

    // buildType, I, N
    private String              buildType;

    // Comma separated list of drop tokens
    private String              dropTokenList;

    // Location of the xml files
    private String              xmlDirectoryName;

    // Location of the html files
    private String              htmlDirectoryName;

    // Location of the resulting index.php file.
    private String              dropDirectoryName;

    // Location and name of the template drop index.php file.
    private String              dropTemplateFileName;

    // Name of the HTML fragment file that any testResults.php file will
    // "include".
    // setting to common default.
    private String              testResultsHtmlFileName                = "testResultsTables.html";

    // Name of the generated drop index php file;
    private String              dropHtmlFileName;

    // Arbitrary path used in the index.php page to href the
    // generated .html files.
    private String              hrefTestResultsTargetPath;
    // Arbitrary path used in the index.php page to reference the compileLogs
    private String              hrefCompileLogsTargetPath;
    // Location of compile logs base directory
    private String              compileLogsDirectoryName;
    // Location and name of test manifest file
    private String              testManifestFileName;
    // private static String testsConstant = ".tests";
    // private static int testsConstantLength = testsConstant.length();
    // temporary way to force "missing" list not to be printed (until complete
    // solution found)
    private boolean             doMissingList                          = true;

    private Set<String>         missingManifestFiles                   = Collections.checkedSortedSet(new TreeSet<>(), String.class);

    class ExpectedConfigFiler implements FilenameFilter {

        String configEnding;

        public ExpectedConfigFiler(String expectedConfigEnding) {
            configEnding = expectedConfigEnding;
        }

        @Override
        public boolean accept(File dir, String name) {
            return (name.endsWith(configEnding));
        }

    }

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
        log(EOL + "ERROR: " + e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            log(stackTraceElement.toString());
        }
    }

    private static byte[] getFileByteContent(final String fileName) throws IOException {
        final File file = new File(fileName);
        try (InputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            return getInputStreamAsByteArray(stream, (int) file.length());
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
    private static byte[] getInputStreamAsByteArray(final InputStream stream, final int length) throws IOException {
        byte[] contents;
        if (length == -1) {
            contents = new byte[0];
            int contentsLength = 0;
            int amountRead = -1;
            do {
                final int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE);

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
        if (Boolean.FALSE) {
            test.setTestsConfigExpected(
                    "ep46I-unit-cen64-gtk2_linux.gtk.x86_64_8.0, ep46I-unit-cen64-gtk3_linux.gtk.x86_64_8.0 ,ep46I-unit-mac64_macosx.cocoa.x86_64_8.0 ,ep46I-unit-win32_win32.win32.x86_8.0");
            DEBUG = true;
            try {
                test.getTestsConfig();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            test.setTestsConfigExpected(
                    "ep46I-unit-cen64-gtk2_linux.gtk.x86_64_8.0 ,ep46I-unit-mac64_macosx.cocoa.x86_64_8.0 ,ep46I-unit-win32_win32.win32.x86_8.0, ep46I-unit-cen64-gtk3_linux.gtk.x86_64_8.0");
            // "%equinox%,%framework%,%extrabundles%,%other%,%incubator%,%provisioning%,%launchers%,%osgistarterkits%");
            test.setDropTokenList(
                    "%sdk%,%tests%,%example%,%rcpruntime%,%rcpsdk%,%runtime%,%jdt%,%jdtsdk%,%jdtc%,%pde%,%pdesdk%,%cvs%,%cvssdk%,%swt%,%relengtools%");
            test.getDropTokensFromList(test.dropTokenList);
            test.setIsBuildTested(false);
            test.setXmlDirectoryName(
                    "/data/shared/eclipse/buildsmirror/4I/siteDir/eclipse/downloads/drops4/N20160614-2120/testresults/xml");
            test.setHtmlDirectoryName(
                    "/data/shared/eclipse/buildsmirror/4I/siteDir/eclipse/downloads/drops4/N20160614-2120/testresults/html");
            test.setDropDirectoryName("/shared/eclipse/buildsmirror/4N/siteDir/eclipse/downloads/drops4/N20160614-2120");

            test.setDropTemplateFileName(
                    "/home/davidw/gitNeon/eclipse.platform.releng.aggregator/eclipse.platform.releng.tychoeclipsebuilder/eclipse/publishingFiles/templateFiles/index.template.php");
            test.setTestResultsHtmlFileName("testResultsTables.html");
            test.setDropHtmlFileName("index.php");

            test.setHrefTestResultsTargetPath("testresults");
            test.setCompileLogsDirectoryName(
                    "/data/shared/eclipse/buildsmirror/4I/siteDir/eclipse/downloads/drops4/N20160614-2120/compilelogs/plugins");
            test.setHrefCompileLogsTargetPath("compilelogs/plugins/");
            test.setTestManifestFileName(
                    "/home/davidw/gitNeon/eclipse.platform.releng.aggregator/eclipse.platform.releng.tychoeclipsebuilder/eclipse/publishingFiles/testManifest.xml");
            test.execute();
        }
    }

    // Configuration of test machines.
    // Add or change new configurations here
    // and update titles in testResults.template.php.
    // These are the suffixes used for JUnit's XML output files.
    // On each invocation, all files in results directory are
    // scanned, to see if they end with suffixes, and if so,
    // are processed for summary row. The column order is determined by
    // the order listed here.
    // This suffix is determined, at test time, when the files junit files are
    // generated, by the setting of a variable named "platform" in test.xml
    // and associated property files.

    // no defaults set since adds to confusion or errors
    // private String[] testsConfigDefaults = { "ep4" + getTestedBuildType() +
    // "-unit-cen64-gtk2_linux.gtk.x86_64_8.0.xml",
    // "ep4" + getTestedBuildType() + "-unit-mac64_macosx.cocoa.x86_64_8.0.xml",
    // "ep4" + getTestedBuildType() + "-unit-win32_win32.win32.x86_8.0.xml",
    // "ep4" + getTestedBuildType() + "-unit-cen64-gtk3_linux.gtk.x86_64_8.0.xml" };
    private String  testsConfigExpected;
    private boolean testRan;
    private String  compilerSummaryFilename = "compilerSummary.html";
    /*
     * Default for "regenerate" is FALSE, but during development, is handy to
     * set to TRUE. If TRUE, the "index.php" file and "compilerSummary.html"
     * files are regenerated. In production that should seldom be required. The
     * testResultsTables.html file, however, is regenerated each call (when
     * 'isTested" is set) since the purpose is usually to include an additional
     * tested platform.
     */
    private boolean regenerate              = Boolean.FALSE;

    private int countCompileErrors(final String aString) {
        return extractNumber(aString, "error");
    }

    private int countCompileWarnings(final String aString) {
        return extractNumber(aString, "warning");
    }

    private int countDiscouragedWarnings(final String aString) {
        return extractNumber(aString, "Discouraged access:");
    }

    private int countInfos(final String aString) {
        return extractNumber(aString, "info");
    }

    /*
     * returns number of errors plus number of failures. returns a negative
     * number if the file is missing or something is wrong with the file (such
     * as is incomplete).
     */
    private int countErrors(final String fileName) {
        int errorCount = -99;
        // File should exists, since we are "driving" this based on file list
        // ... but, just in case.
        if (!new File(fileName).exists()) {
            errorCount = -1;
        } else {

            if (new File(fileName).length() == 0) {
                errorCount = -2;
            } else {

                try {
                    final DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
                    parser = docBuilderFactory.newDocumentBuilder();

                    final Document document = parser.parse(fileName);
                    final NodeList elements = document.getElementsByTagName(elementName);

                    final int elementCount = elements.getLength();
                    if (elementCount == 0) {
                        errorCount = -3;
                    } else {
                        // There can be multiple "testSuites" per file so we
                        // need to
                        // loop through each to count all errors and failures.
                        errorCount = 0;
                        for (int i = 0; i < elementCount; i++) {
                            final Element element = (Element) elements.item(i);
                            final NamedNodeMap attributes = element.getAttributes();
                            Node aNode = attributes.getNamedItem("errors");
                            if (aNode != null) {
                                errorCount = errorCount + Integer.parseInt(aNode.getNodeValue());
                            }
                            aNode = attributes.getNamedItem("failures");
                            errorCount = errorCount + Integer.parseInt(aNode.getNodeValue());
                        }
                    }

                }
                catch (final IOException e) {
                    log(EOL + "ERROR: IOException: " + fileName);
                    logException(e);
                    errorCount = -4;
                }
                catch (final SAXException e) {
                    log(EOL + "ERROR: SAXException: " + fileName);
                    logException(e);
                    errorCount = -5;
                }
                catch (final ParserConfigurationException e) {
                    logException(e);
                    errorCount = -6;
                }
            }
        }
        return errorCount;
    }

    private int countForbiddenWarnings(final String aString) {
        return extractNumber(aString, "Access restriction:");
    }

    @Override
    public void execute() {

        log(EOL + "INFO: Processing test and build results for ");
        log("\t" + getDropDirectoryName());
        anErrorTracker = new ErrorTracker();
        anErrorTracker.loadFile(getTestManifestFileName());
        getDropTokensFromList(getDropTokenList());
        dropTemplateString = readFile(getDropTemplateFileName());

        writeDropIndexFile();

        try {
            parseCompileLogs();
        }
        catch (IOException e) {
            throw new BuildException("Error while parsing Compiler Results File ", e);
        }

        if (isBuildTested()) {

            try {
                parseJUnitTestsXml();

            }
            catch (IOException e) {
                throw new BuildException("Error while parsing JUnit Tests Results Files", e);
            }

        } else {
            log(EOL + "INFO: isBuildTested value was not true, so did no processing for test files");
        }
        log(EOL + "INFO: Completed processing test and build results");
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
            final int discouragedAccessesWarningsCount, final int infoCount, final StringBuilder buffer) {

        if ((forbiddenAccessesWarningsCount == 0) && (discouragedAccessesWarningsCount == 0) && (infoCount == 0)) {
            return;
        }

        String relativeName = computeRelativeName(fileName);
        String shortName = computeShortName(relativeName);

        buffer.append("<tr>").append(EOL).append("<td class='namecell'>").append(EOL).append("<a href=").append("\"")
        .append(relativeName).append("\">").append(shortName).append("</a>").append("</td>\n")
        .append("<td class=\"cell\" >").append("<a href=").append("\"").append(relativeName).append("#FORBIDDEN_WARNINGS")
        .append("\">").append(forbiddenAccessesWarningsCount).append("</a>").append("</td>").append(EOL)
        .append("<td class=\"cell\" >").append("<a href=").append("\"").append(relativeName).append("#DISCOURAGED_WARNINGS")
        .append("\">").append(discouragedAccessesWarningsCount).append("</a>").append("</td>").append(EOL)
        .append("<td class=\"cell\" >").append("<a href=").append("\"").append(relativeName).append("#INFO_WARNINGS")
        .append("\">").append(infoCount).append("</a>").append("</td>").append(EOL).append("</tr>")
        .append(EOL);
    }

    private String computeRelativeName(final String fileName) {
        String relativeName;
        final int i = fileName.indexOf(getHrefCompileLogsTargetPath());
        relativeName = fileName.substring(i);
        return relativeName;
    }

    private String computeShortName(final String relativeName) {
        String shortName;

        int start = getHrefCompileLogsTargetPath().length();
        int last = relativeName.lastIndexOf("/");
        // if there is no "last slash", that's a pretty weird case, but we'll
        // just
        // take the whole rest of string in that case.
        if (last == -1) {
            shortName = relativeName.substring(start);
        } else {
            shortName = relativeName.substring(start, last);
        }
        // further shortening (may need to "back out", so left as separate step)
        // we always expect the name to start with "org.eclipse." .. but, just
        // in case that changes, we'll check and handle if not.
        String commonnamespace = "org.eclipse.";
        if (shortName.startsWith(commonnamespace)) {
            start = commonnamespace.length();
        } else {
            start = 0;
        }
        // Similarly, we alwasy expect the name to end with '_version', but just
        // in case not.
        last = shortName.indexOf('_');
        if (last == -1) {
            shortName = shortName.substring(start);
        } else {
            shortName = shortName.substring(start, last);
        }
        return shortName;
    }

    private void formatCompileErrorRow(final String fileName, final int errorCount, final int warningCount,
            final StringBuilder buffer) {

        if ((errorCount == 0) && (warningCount == 0)) {
            return;
        }

        String relativeName = computeRelativeName(fileName);
        String shortName = computeShortName(relativeName);

        buffer.append("<tr>" + EOL + "<td class='cellname'>" + EOL).append("<a href=").append("\"").append(relativeName)
        .append("\">").append(shortName).append("</a>").append("</td>\n").append("<td class=\"cell\" >").append("<a href=")
        .append("\"").append(relativeName).append("#ERRORS").append("\">").append(errorCount).append("</a>")
        .append("</td>\n").append("<td class=\"cell\" >").append("<a href=").append("\"").append(relativeName)
        .append("#OTHER_WARNINGS").append("\">").append(warningCount).append("</a>").append("</td>\n").append("</tr>\n");
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
    public Vector<String> getDropTokens() {
        return dropTokens;
    }

    private void getDropTokensFromList(final String list) {
        final StringTokenizer tokenizer = new StringTokenizer(list, ",");
        dropTokens = new Vector<>();

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

    private void parseCompileLog(final String log, final StringBuilder compilerLog, final StringBuilder accessesLog) {
        int errorCount = 0;
        int warningCount = 0;
        int forbiddenWarningCount = 0;
        int discouragedWarningCount = 0;
        int infoCount = 0;

        final File file = new File(log);
        Document aDocument = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))){
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
                } else if (INFO_SEVERITY.equals(severityNodeValue)) {
                    // this is an info warning
                    infoCount++;
                }
            }
        }
        if (errorCount != 0) {
            // use wildcard in place of version number on directory names
            // log(log + "/n");
            String logName = log.substring(getCompileLogsDirectoryName().length() + 1);
            final StringBuilder buffer = new StringBuilder(logName);
            buffer.replace(logName.indexOf("_") + 1, logName.indexOf(File.separator, logName.indexOf("_") + 1), "*");
            logName = new String(buffer);

            anErrorTracker.registerError(logName);
        }
        // make sure '.xml' extension is "last thing" in string. (bug 490320)
        final String logName = log.replaceAll(XML_EXTENSION + "$", HTML_EXTENSION);
        formatCompileErrorRow(logName, errorCount, warningCount, compilerLog);
        formatAccessesErrorRow(logName, forbiddenWarningCount, discouragedWarningCount, infoCount, accessesLog);
    }

    private void parseCompileLogs() throws IOException {
        File sourceDirectory = new File(getCompileLogsDirectoryName());
        File mainDir = new File(getDropDirectoryName());
        File compilerSummaryFile = new File(mainDir, compilerSummaryFilename);
        // we do not recompute compiler summary each time, since it is
        // fairly time consuming -- and no reason it would not be "complete",
        // if it exists.
        if (compilerSummaryFile.exists() && !isRegenerate()) {
            log(EOL + "INFO: Compile logs summary page, " + compilerSummaryFilename
                    + ", was found to exist already and not regenerated.");
        } else {
            if (compilerSummaryFile.exists()) {
                log(EOL + "INFO: Compile logs summary page, " + compilerSummaryFilename
                        + ", was found to exist already and is being regenerated.");
            }
            log("DEBUG: BEGIN: Parsing compile logs and generating summary table.");
            String compileLogResults = "";
            final StringBuilder compilerString = new StringBuilder();
            final StringBuilder accessesString = new StringBuilder();
            processCompileLogsDirectory(getCompileLogsDirectoryName(), compilerString, accessesString);
            if (compilerString.length() == 0) {
                compilerString.append(
                        "<tr><td class='namecell'>None</td><td class='cell'>&nbsp;</td><td class='cell'>&nbsp;</td></tr>" + EOL);
            }
            if (accessesString.length() == 0) {
                accessesString.append(
                        "<tr><td class='namecell'>None</td><td class='cell'>&nbsp;</td><td class='cell'>&nbsp;</td></tr>" + EOL);
            }

            compileLogResults = EOL + EOL + "<h3 id=\"PluginsErrors\">Plugins containing compile errors or warnings</h3>" + EOL
                    + EOL
                    + "<p>The table below shows the plugins in which errors or warnings were encountered. Click on the jar file link to view its"
                    + EOL + "detailed report.</p>" + EOL + EOL + "<table>" + EOL + "  <tr>" + EOL
                    + "    <th class='cell'>Compile Logs (Jar Files)</th>" + EOL + "    <th class='cell'>Errors</th>" + EOL
                    + "<th class='cell'>Warnings</th>" + EOL + "  </tr>" + EOL;

            compileLogResults = compileLogResults + compilerString.toString();

            compileLogResults = compileLogResults + "          </table>" + EOL + EOL
                    + "<h3 id=\"AcessErrors\">Plugins containing access errors or warnings</h3>" + EOL + "<table>" + EOL + " <tr>"
                    + EOL + "<th class='cell'>Compile Logs (Jar Files)</th>" + EOL + "   <th class='cell'>Forbidden Access</th>"
                    + EOL + "   <th class='cell'>Discouraged Access</th>" + EOL + "   <th class='cell'>Info Warnings</th>" + EOL + "</tr>" + EOL;

            compileLogResults = compileLogResults + accessesString.toString();
            compileLogResults = compileLogResults + "</table>" + EOL;
            // write the include file. The name of this file must match what is
            // in testResults.template.php
            writePhpIncludeCompilerResultsFile(sourceDirectory, compileLogResults);
            log("DEBUG: End: Parsing compile logs and generating summary table.");
        }
    }

    private void parseJUnitTestsXml() throws IOException {
        log("DEBUG: Begin: Parsing XML JUnit results files");
        ArrayList<String> foundConfigs = new ArrayList<>();
        final File xmlResultsDirectory = new File(getXmlDirectoryName());
        ResultsTable resultsTable = new ResultsTable(getTestsConfig());
        if (xmlResultsDirectory.exists()) {
            // reinitialize each time.
            // We currently "re do" all of tests, but can improve in the future
            // where the "found configs" are remembered, but then have to keep
            // track of original order (not "found" order which has to with when
            // tests completed).
            foundConfigs.clear();

            ArrayList<File> allFileNames = new ArrayList<>();

            for (String expectedConfig : getTestsConfig()) {

                FilenameFilter configfilter = new ExpectedConfigFiler("_" + expectedConfig + XML_EXTENSION);
                // we end with "full" list of files, sorted by configfilter, and
                // then alphabetical.
                File[] xmlFileNamesForConfig = xmlResultsDirectory.listFiles(configfilter);

                if (xmlFileNamesForConfig.length > 0) {
                    // log("DEBUG: For " + expectedConfig + " found " +
                    // xmlFileNamesForConfig.length + " XML results files");
                    foundConfigs.add(expectedConfig);
                    // sort by name, for each 'config' found.
                    Arrays.sort(xmlFileNamesForConfig);
                    for (File file : xmlFileNamesForConfig) {
                        allFileNames.add(file);
                    }
                }
            }
            File[] xmlFileNames = new File[allFileNames.size()];
            allFileNames.toArray(xmlFileNames);
            // files MUST be alphabetical, for now?
            Arrays.sort(xmlFileNames);
            String sourceDirectoryCanonicalPath = getDropDirectoryName();
            for (int i = 0; i < xmlFileNames.length; i++) {
                File junitResultsFile = xmlFileNames[i];
                checkIfMissingFromTestManifestFile(junitResultsFile, foundConfigs);
                String fullName = junitResultsFile.getPath();
                int errorCount = countErrors(fullName);
                resultsTable.putCell(computeCoreName(junitResultsFile), computeConfig(junitResultsFile), errorCount,
                        junitResultsFile);
                if (errorCount != 0) {
                    trackDataForMail(sourceDirectoryCanonicalPath, junitResultsFile, fullName);
                }
            }
        } else {
            // error? Or, just too early?
            log(EOL + "WARNING: sourceDirectory did not exist at \n\t" + xmlResultsDirectory);
            log("     either incorrect call to 'generate index' or called too early (tests not done yet)?");
        }
        log("DEBUG: End: Parsing XML JUnit results files");
        // above is all "compute data". Now it is time to "display" it.
        if (foundConfigs.size() > 0) {
            log("DEBUG: Begin: Generating test results index tables in " + getTestResultsHtmlFileName());
            setTestsRan(true);
            writeHTMLResultsTable(foundConfigs, resultsTable);
            log("DEBUG: End: Generating test results index tables");
        } else {
            setTestsRan(false);
            log(EOL + "WARNING: Test results not found in " + xmlResultsDirectory.getAbsolutePath());
        }

    }

    private void writeHTMLResultsTable(ArrayList<String> foundConfigs, ResultsTable resultsTable) throws IOException {
        // These first files reflect what we expected, and what we found.
        String found_config_type = "found";
        writePhpConfigFile(found_config_type, foundConfigs, getFoundConfigFilename());
        // write the table to main output directory in testResultsTables.html,
        // which
        // in turn is included by the testResults.php file.

        String htmlString = "";
        // first we right a bit of "static" part. That comes before the table.
        htmlString = htmlString + EOL + "<h3 id=\"UnitTest\">Unit Test Results</h3>" + EOL;

        htmlString = htmlString
                + "<p>The unit tests are run on the <a href=\"https://ci.eclipse.org/releng/job/AutomatedTests/\">releng ci instance</a>.</p>"
                + EOL;

        htmlString = htmlString + "<p>The table shows the unit test results for this build on the platforms" + EOL;
        htmlString = htmlString + "tested. You may access the test results page specific to each" + EOL;
        htmlString = htmlString + "component on a specific platform by clicking the cell link." + EOL;
        htmlString = htmlString + "Normally, the number of errors is indicated in the cell.</p>" + EOL;
        htmlString = htmlString + "<p>A negative number or \"DNF\" means the test \"Did Not Finish\" for unknown reasons" + EOL;
        htmlString = htmlString + "and hence no results page is available. In that case," + EOL;
        htmlString = htmlString + "more information can sometimes be found in" + EOL;
        htmlString = htmlString + "the <a href=\"logs.php#console\">console logs</a>.</p>" + EOL;
        htmlString = htmlString + "<?php" + EOL;
        htmlString = htmlString + "if (file_exists(\"testNotes.html\")) {" + EOL;
        htmlString = htmlString + "  $my_file = file_get_contents(\"testNotes.html\");" + EOL;
        htmlString = htmlString + "  echo $my_file;" + EOL;
        htmlString = htmlString + "}" + EOL;
        htmlString = htmlString + "?>" + EOL;

        htmlString = htmlString + startTableOfUnitResults();
        for (String row : resultsTable) {
            htmlString = htmlString + formatJUnitRow(row, resultsTable, foundConfigs);
        }
        // Once we are done with the Unit tests rows, we must add end table
        // tag, since the following methods may or may not add a table of
        // their own.
        htmlString = htmlString + EOL + "</table>" + EOL;
        // check for missing test logs
        // TODO put styling on these tables
        htmlString = htmlString + verifyAllTestsRan(xmlDirectoryName, foundConfigs);
        htmlString = htmlString + listMissingManifestFiles();
        writeTestResultsFile(htmlString);
    }

    private String startTableOfUnitResults() throws IOException {
        String result = "";
        int ncolumns = getTestsConfig().size();
        result = result + "<table>" + EOL;
        // table header
        result = result + "<tr>" + EOL;
        result = result + "<th class='cell' " + " rowspan='2' > org.eclipse <br /> Test Bundles </th>" + EOL;
        result = result + "<th class='cell' colspan='" + ncolumns + "'> Test Configurations (Hudson Job/os.ws.arch/VM) </th>" + EOL;
        result = result + "</tr>\n";

        result = result + "<tr>" + EOL;

        for (String column : getTestsConfig()) {
            result = result + "<th class='cell'>" + computeDisplayConfig(column) + "</th>\n";
        }
        result = result + "</tr>" + EOL;
        // end table header
        return result;
    }

    /*
     * This function "breaks" the full config string at meaningful underscores,
     * for improved display in tables and similar. Remember, some config values
     * can have more than two underscores, such as
     * ep46I-unit-lin64_linux.gtk.x86_64_8.0, which should be split as
     * ep46I-unit-lin64 lin64_linux.gtk.x86_64 8.0
     */
    private String computeDisplayConfig(String config) {
        int lastUnderscore = config.lastIndexOf("_");
        int firstUnderscore = config.indexOf('_');
        // echo "<br/>DEBUG: config: config firstUnderscore: firstUnderscore
        // lastUnderscore: lastUnderscore lastMinusFirst: platformLength"
        String jobname = config.substring(0, firstUnderscore);
        String platformconfig = config.substring(firstUnderscore + 1, lastUnderscore);
        String vmused = config.substring(lastUnderscore + 1);
        // echo "DEBUG: jobname: ".jobname."<br/>";
        // echo "DEBUG: platformconfig: ".platformconfig."<br/>";
        // echo "DEBUG: vmused: ".vmused."<br/>";
        return jobname + "<br/>" + platformconfig + "<br/>" + vmused;

    }

    private void setTestsRan(boolean b) {
        testRan = b;
    }

    /*
     * As far as I know, this "work" was done to track data send out in an
     * email.
     */
    private void trackDataForMail(String sourceDirectoryCanonicalPath, File junitResultsFile, final String fullName) {
        final String testName = junitResultsFile.getName().substring(0,
                junitResultsFile.getName().length() - XML_EXTENSION.length());
        testResultsWithProblems = testResultsWithProblems.concat(EOL + testName);
        testResultsXmlUrls = testResultsXmlUrls
                .concat(EOL + extractXmlRelativeFileName(sourceDirectoryCanonicalPath, junitResultsFile));
        anErrorTracker.registerError(fullName.substring(getXmlDirectoryName().length() + 1));
    }

    /*
     * This is the "reverse" of checking for "missing test results". It is
     * simple sanity check to see if all "known" test results are listed in in
     * the testManifest.xml file. We only do this check if we also are checking
     * for missing logs which depends on an accurate testManifest.xml file.
     */
    private void checkIfMissingFromTestManifestFile(File junitResultsFile, ArrayList<String> foundConfigs) {
        if (getDoMissingList()) {
            if (!verifyLogInManifest(junitResultsFile.getName(), foundConfigs)) {
                String corename = computeCoreName(junitResultsFile);
                missingManifestFiles.add(corename);
            }
        }
    }

    private String computeCoreName(File junitResultsFile) {
        String fname = junitResultsFile.getName();
        // corename is all that needs to be listed in testManifest.xml
        String corename = null;
        int firstUnderscorepos = fname.indexOf('_');
        if (firstUnderscorepos == -1) {
            // should not occur, but if it does, we will take whole name
            corename = fname;
        } else {
            corename = fname.substring(0, firstUnderscorepos);
        }
        return corename;
    }

    private String computeConfig(File junitResultsFile) {
        String fname = junitResultsFile.getName();
        String configName = null;
        int firstUnderscorepos = fname.indexOf('_');
        if (firstUnderscorepos == -1) {
            // should not occur, but if it does, we will set to null
            // and let calling program decide what to do.
            configName = null;
        } else {
            int lastPos = fname.lastIndexOf(XML_EXTENSION);
            if (lastPos == -1) {
                configName = null;
            } else {
                configName = fname.substring(firstUnderscorepos + 1, lastPos);
            }
        }
        return configName;
    }

    private void writePhpConfigFile(String config_type, ArrayList<String> configs, String phpfilename) throws IOException {
        File mainDir = new File(getDropDirectoryName());
        File testConfigsFile = new File(mainDir, phpfilename);
        try (Writer testconfigsPHP = new FileWriter(testConfigsFile)) {
            testconfigsPHP.write("<?php" + EOL);
            testconfigsPHP.write("//This file created by 'generateIndex' ant task, while parsing test results" + EOL);
            testconfigsPHP.write("// It is based on " + config_type + " testConfigs" + EOL);
            String phpArrayVariableName = "$" + config_type + "TestConfigs";
            testconfigsPHP.write(phpArrayVariableName + " = array();" + EOL);
            for (String fConfig : configs) {
                testconfigsPHP.write(phpArrayVariableName + "[]=\"" + fConfig + "\";" + EOL);
            }
        }
    }

    private void writePhpIncludeCompilerResultsFile(final File sourceDirectory, String compilerSummary) throws IOException {
        File mainDir = new File(getDropDirectoryName());
        File compilerSummaryFile = new File(mainDir, compilerSummaryFilename);
        try (Writer compilerSummaryPHP = new FileWriter(compilerSummaryFile)) {
            compilerSummaryPHP.write("<!--" + EOL);
            compilerSummaryPHP
            .write("  This file created by 'generateIndex' ant task, while parsing build and tests results" + EOL);
            compilerSummaryPHP.write("-->" + EOL);
            compilerSummaryPHP.write(compilerSummary);
        }
    }

    private void processCompileLogsDirectory(final String directoryName, final StringBuilder compilerLog,
            final StringBuilder accessesLog) {
        final File sourceDirectory = new File(directoryName);
        if (sourceDirectory.isFile()) {
            if (sourceDirectory.getName().endsWith(".log")) {
                readCompileLog(sourceDirectory.getAbsolutePath(), compilerLog, accessesLog);
            }
            if (sourceDirectory.getName().endsWith(XML_EXTENSION)) {
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

    private String processDropRow(final PlatformStatus aPlatform) {
        if ("equinox".equalsIgnoreCase(aPlatform.getFormat())) {
            return processEquinoxDropRow(aPlatform);
        } else {
            return processEclipseDropRow(aPlatform);
        }

    }

    private String processEclipseDropRow(PlatformStatus aPlatform) {
        String result = "<tr>\n<td>" + aPlatform.getName() + "</td>\n";
        // generate file link, size and checksums in the php template
        result = result + "<?php genLinks(\"" + aPlatform.getFileName() + "\"); ?>\n";
        result = result + "</tr>\n";
        return result;
    }

    private String processDropRows(final PlatformStatus[] platforms) {
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
    private String processEquinoxDropRow(final PlatformStatus aPlatform) {
        String result = "<tr>";
        result = result + "<td>";
        final String filename = aPlatform.getFileName();
        // if there are images, put them in the same table column as the name of
        // the file
        final List<String> images = aPlatform.getImages();
        if ((images != null) && !images.isEmpty()) {
            for (final Iterator<String> iter = images.iterator(); iter.hasNext();) {
                result = result + "<img src=\"" + iter.next() + "\"/>&nbsp;";
            }
        }
        result = result + "<a href=\"download.php?dropFile=" + filename + "\">" + filename + "</a></td>\n";
        result = result + "{$generateDropSize(\"" + filename + "\")}\n";
        result = result + "{$generateChecksumLinks(\"" + filename + "\", $buildlabel)}\n";
        result = result + "</tr>\n";
        return result;
    }

    private void readCompileLog(final String log, final StringBuilder compilerLog, final StringBuilder accessesLog) {
        final String fileContents = readFile(log);

        final int errorCount = countCompileErrors(fileContents);
        final int warningCount = countCompileWarnings(fileContents);
        final int forbiddenWarningCount = countForbiddenWarnings(fileContents);
        final int discouragedWarningCount = countDiscouragedWarnings(fileContents);
        final int infoCount = countInfos(fileContents);
        if (errorCount != 0) {
            // use wildcard in place of version number on directory names
            String logName = log.substring(getCompileLogsDirectoryName().length() + 1);
            final StringBuilder stringBuilder = new StringBuilder(logName);
            stringBuilder.replace(logName.indexOf("_") + 1, logName.indexOf(File.separator, logName.indexOf("_") + 1), "*");
            logName = new String(stringBuilder);

            anErrorTracker.registerError(logName);
        }
        formatCompileErrorRow(log, errorCount, warningCount, compilerLog);
        formatAccessesErrorRow(log, forbiddenWarningCount, discouragedWarningCount, infoCount, accessesLog);
    }

    private String readFile(final String fileName) {
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
            log(EOL + "WARNING: Could not find token: " + original);
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
    public void setDropTokens(final Vector<String> vector) {
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

    /**
     * @param string
     */
    public void setTestResultsWithProblems(final String string) {
        testResultsWithProblems = string;
    }

    public void setXmlDirectoryName(final String aString) {
        xmlDirectoryName = aString;
    }

    private String verifyAllTestsRan(final String directory, ArrayList<String> foundConfigs) {
        String replaceString = "";
        ArrayList<String> missingFiles = new ArrayList<>();
        if (getDoMissingList()) {
            for (String testLogName : anErrorTracker.getTestLogs(foundConfigs)) {

                if (new File(directory + File.separator + testLogName).exists()) {
                    // log("DEBUG: found log existed: " + testLogName);
                    continue;
                }
                // log("DEBUG: found log DID NOT exist: " + testLogName);
                anErrorTracker.registerError(testLogName);
                // replaceString = replaceString + tmp;
                // testResultsWithProblems appears to be for email, or similar?
                testResultsWithProblems = testResultsWithProblems
                        .concat(EOL + testLogName.substring(0, testLogName.length() - XML_EXTENSION.length()) + " (file missing)");
                missingFiles.add(testLogName);
            }
        } else {
            // Note: we intentionally do not deal with missing file for perf.
            // tests yet.
            // (though, probably could, once fixed with "expected configs").
            replaceString = replaceString + "<tbody>\n" + "<tr><td colspan=\"0\"><p><span class=\"footnote\">NOTE: </span>\n"
                    + "Remember that for performance unit test tables, there are never any \"missing files\" listed, if there are any. \n"
                    + "This is expected to be a temporary solution, until an exact fix can be implemented. For more details, see \n"
                    + "<a href=\"https://bugs.eclipse.org/bugs/show_bug.cgi?id=451890\">bug 451890</a>.</p>\n" + "</td></tr>\n"
                    + "</tbody>\n";
        }
        // TODO: we need lots more of separating "data" from "formating"
        if (getDoMissingList() && missingFiles.size() > 0) {
            String ordinalWord = "File";
            if (missingFiles.size() > 1) {
                ordinalWord = "Files";
            }

            replaceString = replaceString + "</table>" + EOL + "<table>" + "<tr> <th class='cell'>Missing " + ordinalWord
                    + "</th></tr>";
            for (String testLogName : missingFiles) {
                replaceString = replaceString + EOL + "<tr><td class='namecell'>" + testLogName + "</td></tr>";
            }
            replaceString = replaceString + EOL + "</table>";
        }
        return replaceString;
    }

    private void writeDropIndexFile() {
        final String outputFileName = getDropDirectoryName() + File.separator + getDropHtmlFileName();
        File outputIndexFile = new File(outputFileName);
        // we assume if "eclipse" has been done, then "equinox" has been as
        // well.
        if (outputIndexFile.exists() && !isRegenerate()) {
            log(EOL + "INFO: The drop index file, " + getDropHtmlFileName() + ", was found to exist already and not regenerated.");
        } else {
            if (outputIndexFile.exists()) {
                log(EOL + "INFO: The drop index file, " + getDropHtmlFileName()
                + ", was found to exist already and is being regenerated.");
            }
            log("DEBUG: Begin: Generating drop index page");
            final String[] types = anErrorTracker.getTypes();
            for (int i = 0; i < types.length; i++) {
                final PlatformStatus[] platforms = anErrorTracker.getPlatforms(types[i]);
                final String replaceString = processDropRows(platforms);
                dropTemplateString = replace(dropTemplateString, dropTokens.get(i).toString(), replaceString);
            }
            writeFile(outputIndexFile, dropTemplateString);
            log("DEBUG: End: Generating drop index page");
        }
    }

    private void writeFile(File outputFile, final String contents) {
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))){
            outputStream.write(contents.getBytes());
        }
        catch (final FileNotFoundException e) {
            log(EOL + "ERROR: File not found exception while writing: " + outputFile.getPath());
        }
        catch (final IOException e) {
            log(EOL + "ERROR: IOException writing: " + outputFile.getPath());
        }
    }

    /*
     * This method writes the computed HTML to the file specified by caller in
     * testResultsHtmlFileName. There must be an appropriate file on Download
     * site that "includes" the file.
     *
     * @param contents
     */
    private void writeTestResultsFile(String contents) {
        final String outputFileName = dropDirectoryName + File.separator + testResultsHtmlFileName;
        File outputFile = new File(outputFileName);
        writeFile(outputFile, contents);

    }

    public String getTestsConfigExpected() {

        return testsConfigExpected;
    }

    public void setTestsConfigExpected(String testsConfigExpected) {
        this.testsConfigExpected = testsConfigExpected;
        // log("DEBUG: testsConfigExpected: " + testsConfigExpected);
    }

    private ArrayList<String> getTestsConfig() throws IOException {
        if (expectedConfigs == null) {
            expectedConfigs = new ArrayList<>();
            String expectedConfigParam = getTestsConfigExpected();
            if (expectedConfigParam != null) {
                StringTokenizer tokenizer = new StringTokenizer(expectedConfigParam, " ,\t");
                while (tokenizer.hasMoreTokens()) {
                    expectedConfigs.add(tokenizer.nextToken());
                }
            } else {
                throw new BuildException("test configurations were not found. One or more must be set.");
            }
            if (DEBUG) {
                // log("DEBUG: testsConfig array ");
                for (String expected : expectedConfigs) {
                    log("\tDEBUG: expectedTestConfig: " + expected);
                }
            }
            // write expected test config file here. This file is later used by
            // the PHP file so the name passed in must match what was put in PHP
            // file.
            writePhpConfigFile(expected_config_type, expectedConfigs, getExpectedConfigFilename());
        }
        return expectedConfigs;
    }

    public boolean getDoMissingList() {
        return doMissingList;
    }

    public void setDoMissingList(boolean doMissingList) {
        this.doMissingList = doMissingList;
    }

    /*
     * This is the reverse of checking that all expected logs were found. If
     * logs were found that are NOT in the test manifest, we write the list
     * below missing files, so that they can be added to testManifest.xml. This
     * allows them to be detected as missing, in future. We only do this check
     * if "doMissingList" is true.
     */
    private boolean verifyLogInManifest(String filename, ArrayList<String> foundConfigs) {
        boolean result = false;
        if (getDoMissingList()) {
            for (String testLogName : anErrorTracker.getTestLogs(foundConfigs)) {
                if (filename.equals(testLogName)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    private String listMissingManifestFiles() throws IOException {
        String results = "";
        if (getDoMissingList()) {
            String xmlFragment = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + EOL + "<topLevel>" + EOL;

            if (getDoMissingList() && missingManifestFiles.size() > 0) {
                String ordinalWord = "File";
                if (missingManifestFiles.size() > 1) {
                    ordinalWord = "Files";
                }

                results = results + EOL + "<table>"
                        + "<tr> <th class='cell'>Releng: <a href=\"addToTestManifest.xml\">Missing testManifest.xml " + ordinalWord
                        + "</a></th></tr>";
                for (String testLogName : missingManifestFiles) {
                    results = results + EOL + "<tr><td class='namecell'>" + testLogName + "</td></tr>";
                    xmlFragment = xmlFragment + "<logFile " + EOL + "  name=\"" + testLogName + "\"" + EOL + "  type=\"test\" />"
                            + EOL;
                }
                results = results + EOL + "</table>";
                xmlFragment = xmlFragment + "</topLevel>";
                try (FileWriter xmlOutput = new FileWriter(getDropDirectoryName() + "/addToTestManifest.xml")) {
                    xmlOutput.write(xmlFragment);
                }
            }
        }
        return results;
    }

    // Specific to the RelEng test results page
    private String formatJUnitRow(String corename, ResultsTable resultsTable, ArrayList<String> foundConfigs) throws IOException {

        String results = "";
        int orgEclipseLength = "org.eclipse.".length();
        // indexOf('_') assumes never part of file name?
        final String displayName = corename.substring(orgEclipseLength);

        results = results + EOL + "<tr><td class=\"namecell\">" + displayName + "</td>";

        for (String config : getTestsConfig()) {
            Cell cell = resultsTable.getCell(corename, config);
            if (cell == null && foundConfigs.contains(config)) {
                cell = resultsTable.new Cell(-1, null);
            }
            results = results + printCell(cell);
        }
        results = results + "</tr>" + EOL;
        return results;
    }

    private String printCell(Cell cell) {
        String result = null;
        String displayName = null;
        if (cell == null) {
            displayName = "<td class=\"cell\">&nbsp;</td>";
            result = displayName;
        } else {
            int cellErrorCount = cell.getErrorCount();
            File cellResultsFile = cell.getResultsFile();
            String filename = null;
            int beginFilename = 0;
            String rawfilename = null;
            if (cellResultsFile != null) {
                filename = cellResultsFile.getName();
                beginFilename = filename.lastIndexOf(File.separatorChar);
                rawfilename = filename.substring(beginFilename + 1, filename.length() - XML_EXTENSION.length());
            }
            String startCell = null;
            if (cellErrorCount == -999) {
                displayName = "<td class=\"cell\">&nbsp;</td>";
                result = displayName;
            } else if (cellErrorCount == 0) {
                startCell = "<td class=\"cell\">";
                displayName = "(0)";
                result = addLinks(startCell, displayName, rawfilename);
            } else if (cellErrorCount < 0) {
                startCell = "<td class=\"errorcell\">";
                displayName = "(" + Integer.toString(cellErrorCount) + ") DNF ";
                result = startCell + displayName + "</td>";
            } else if (cellErrorCount > 0) {
                startCell = "<td class=\"errorcell\">";
                displayName = "(" + Integer.toString(cellErrorCount) + ")";
                result = addLinks(startCell, displayName, rawfilename);
            } else {
                // should never occur
                displayName = "<td class='errorcell'>?" + Integer.toString(cellErrorCount) + "?</td>";
                result = displayName;
            }
        }
        return result;

    }

    private String addLinks(String startCell, String displayName, String rawfilename) {
        String result = startCell;
        result = result + "<a style=\"color:inherit\" title=\"Detailed Unit Test Results Table\" href=" + "\""
                + getHrefTestResultsTargetPath() + "/html/" + rawfilename + HTML_EXTENSION + "\">" + displayName + "</a>";
        result = result
                + "<a style=\"color:#555555\" title=\"XML Test Result (e.g. for importing into the Eclipse JUnit view)\" href=\""
                + getHrefTestResultsTargetPath() + "/xml/" + rawfilename + XML_EXTENSION + "\">&nbsp;(XML)</a>";
        return result + "</td>";
    }

    // Totally non-functional method. restored from history for investigation.
    // I restored this 'mailResults' method from history. It was removed about
    // 3.8 M3. It was commented out
    // at that time. Not sure for how long. I am not sure where "Mailer" class
    // was coming from.
    // Needs more research or re-invention. (Compare with CBI aggregator
    // method?)

    void mailResults() {
        Mailer mailer = null;
        // send a different message for the following cases:
        // build is not tested at all // build is tested, tests have not run
        // build is tested, tests have run with error and or failures
        // build is tested, tests have run with no errors or failures
        try {
            mailer = new Mailer();
        }
        catch (NoClassDefFoundError e) {
            return;
        }
        String buildLabel = mailer.getBuildLabel();
        String httpUrl = mailer.getHttpUrl() + "/" + buildLabel; //

        String subject = "Build is complete.  ";

        String downloadLinks = "\n\nHTTP Download:\n\n\t" + httpUrl + " \n\n"; //

        // provide http links
        String message = "The build is complete." + downloadLinks;

        if (testsRan()) {
            subject = "Automated JUnit testing complete.  ";
            message = "Automated JUnit testing is complete.  ";
            subject = subject
                    .concat((getTestResultsWithProblems().endsWith("\n")) ? "All tests pass." : "Test failures/errors occurred.");
            message = message.concat((getTestResultsWithProblems().endsWith("\n")) ? "All tests pass."
                    : "Test failures/errors occurred in the following:  " + getTestResultsWithProblems()) + downloadLinks;
        } else if (isBuildTested() && (!getBuildType().equals("N"))) {
            subject = subject.concat("Automated JUnit testing is starting.");
            message = "The " + subject + downloadLinks;
        }

        if (subject.endsWith("Test failures/errors occurred.")) {
            mailer.sendMessage(subject, message);
        } else if (!getBuildType().equals("N")) {
            mailer.sendMessage(subject, message);
        }
    }

    private boolean testsRan() {
        return testRan;
    }

    public boolean isRegenerate() {
        return regenerate;
    }

    public void setRegenerate(boolean regenerate) {
        this.regenerate = regenerate;
    }

    /* purely a place holder */
    class Mailer {

        public Object getBuildProperties() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getBuildLabel() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getHttpUrl() {
            // TODO Auto-generated method stub
            return null;
        }

        public void sendMessage(String subject, String message) {
            // TODO Auto-generated method stub

        }

    }

    public String getExpectedConfigFilename() {
        if (expectedConfigFilename == null) {
            expectedConfigFilename = EXPECTED_TEST_CONFIGS_FILENAME_DEFAULT;
        }
        return expectedConfigFilename;
    }

    public void setExpectedConfigFilename(String expectedConfigFilename) {
        this.expectedConfigFilename = expectedConfigFilename;
    }

    public String getFoundConfigFilename() {
        if (foundConfigFilename == null) {
            foundConfigFilename = FOUND_TEST_CONFIGS_FILENAME_DEFAULT;
        }
        return foundConfigFilename;
    }

    public void setFoundConfigFilename(String foundConfigFilename) {
        this.foundConfigFilename = foundConfigFilename;
    }

}
