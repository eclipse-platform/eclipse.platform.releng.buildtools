
package org.eclipse.releng.build.tools.comparator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for extracting the relevent "Debug" messages from
 * the huge maven debug log.
 * 
 * @author davidw
 */
public class Extractor {

	public static final String BUILD_DIRECTORY_PROPERTY = "builddirectory";
	public static final String COMPARATOR_REPO_PROPERTY = "comparatorRepo";
	private static final String EOL = System.lineSeparator();

	private final String debugFilename = "mb060_run-maven-build_output.txt";
	private final String outputFilenameFull = "buildtimeComparatorFull.log.txt";
	private final String outputFilenameSign = "buildtimeComparatorSignatureOnly.log.txt";
	private final String outputFilenameSignPlusInnerJar = "buildtimeComparatorSignatureOnlyWithInnerJar.log.txt";
	private final String outputFilenameDoc = "buildtimeComparatorDocBundle.log.txt";
	private final String outputFilenameOther = "buildtimeComparatorUnanticipated.log.txt";
	private final String outputFilenamejdtCore = "buildtimeComparatorJDTCore.log.txt";
	private final String buildlogsDirectory = "buildlogs";
	private final String comparatorLogsDirectory = "comparatorlogs";
	private String comparatorRepo = "comparatorRepo";
	private String buildDirectory;
	private String inputFilename;
	private String outputFilenameFullLog;
	private String outputFilenameSignLog;
	private String outputFilenameDocLog;
	private String outputFilenameOtherLog;
	private String outputFilenameSignPlusInnerJarLog;
	private String outputFilenamejdtCoreLog;
	private final String mainregexPattern = "^\\[WARNING\\].*eclipse.platform.releng.aggregator/(.*): baseline and build artifacts have same version but different contents";
	private final Pattern mainPattern = Pattern.compile(mainregexPattern);
	private final String noclassifierregexPattern = "^.*no-classifier:.*$";
	private final Pattern noclassifierPattern = Pattern.compile(noclassifierregexPattern);
	private final String classifier_sourcesregexPattern = "^.*classifier-sources:.*$";
	private final Pattern classifier_sourcesPattern = Pattern.compile(classifier_sourcesregexPattern);
	private final String classifier_sourcesfeatureregexPattern = "^.*classifier-sources-feature:.*$";
	private final Pattern classifier_sourcesfeaturePattern = Pattern.compile(classifier_sourcesfeatureregexPattern);

	private final String sign1regexPattern = "^.*META-INF/(ECLIPSE_|CODESIGN).RSA.*$";
	private final Pattern sign1Pattern = Pattern.compile(sign1regexPattern);
	private final String sign2regexPattern = "^.*META-INF/(ECLIPSE_|CODESIGN).SF.*$";
	private final Pattern sign2Pattern = Pattern.compile(sign2regexPattern);
	private final String docNameregexPattern = "^.*eclipse\\.platform\\.common.*\\.doc\\..*$";
	private final Pattern docNamePattern = Pattern.compile(docNameregexPattern);
	// jar pattern added for bug 416701
	private final String jarregexPattern = "^.*\\.jar.*$";
	private final Pattern jarPattern = Pattern.compile(jarregexPattern);
	private int count;
	private int countSign;
	private int countDoc;
	private int countOther;
	private int countSignPlusInnerJar;
	private int countJDTCore;

	private boolean docItem(final LogEntry newEntry) {
		boolean result = false;
		final String name = newEntry.getName();
		final Matcher matcher = docNamePattern.matcher(name);
		if (matcher.matches()) {
			result = true;
		}
		return result;
	}

	public String getBuildDirectory() {
		// if not set explicitly, see if its a system property
		if (buildDirectory == null) {
			buildDirectory = System.getProperty(BUILD_DIRECTORY_PROPERTY);
		}
		return buildDirectory;
	}

	private String getInputFilename() {
		if (inputFilename == null) {
			inputFilename = getBuildDirectory() + "/" + buildlogsDirectory + "/" + debugFilename;
		}
		return inputFilename;
	}

	private String getOutputFilenameDoc() {
		if (outputFilenameDocLog == null) {
			outputFilenameDocLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory + "/"
					+ outputFilenameDoc;
		}
		return outputFilenameDocLog;
	}

	private String getOutputFilenameFull() {
		if (outputFilenameFullLog == null) {
			outputFilenameFullLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory + "/"
					+ outputFilenameFull;
		}
		return outputFilenameFullLog;
	}

	private String getOutputFilenameOther() {
		if (outputFilenameOtherLog == null) {
			outputFilenameOtherLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory
					+ "/" + outputFilenameOther;
		}
		return outputFilenameOtherLog;
	}

	private String getOutputFilenameSign() {
		if (outputFilenameSignLog == null) {
			outputFilenameSignLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory + "/"
					+ outputFilenameSign;
		}
		return outputFilenameSignLog;
	}

	private String getOutputFilenameSignWithInnerJar() {
		if (outputFilenameSignPlusInnerJarLog == null) {
			outputFilenameSignPlusInnerJarLog = getBuildDirectory() + "/" + buildlogsDirectory + "/"
					+ comparatorLogsDirectory + "/" + outputFilenameSignPlusInnerJar;
		}
		return outputFilenameSignPlusInnerJarLog;
	}

	private String getOutputFilenameJDTCore() {
		if (outputFilenamejdtCoreLog == null) {
			outputFilenamejdtCoreLog = getBuildDirectory() + "/" + buildlogsDirectory + "/" + comparatorLogsDirectory
					+ "/" + outputFilenamejdtCore;
		}
		return outputFilenamejdtCoreLog;
	}

	public void processBuildfile() throws IOException {

		// Make sure directory exists
		File outputDir = new File(getBuildDirectory() + "/" + buildlogsDirectory, comparatorLogsDirectory);
		if (!outputDir.exists()) {
			outputDir.mkdirs();
		}

		final File infile = new File(getInputFilename());
		final File outfile = new File(getOutputFilenameFull());
		final File outfileSign = new File(getOutputFilenameSign());
		final File outfileDoc = new File(getOutputFilenameDoc());
		final File outfileOther = new File(getOutputFilenameOther());
		final File outfileSignWithInnerJar = new File(getOutputFilenameSignWithInnerJar());
		final File outfileJDTCore = new File(getOutputFilenameJDTCore());
		try (Reader in = new FileReader(infile);
				BufferedReader input = new BufferedReader(in);
				Writer out = new FileWriter(outfile);
				BufferedWriter output = new BufferedWriter(out);
				Writer outsign = new FileWriter(outfileSign);
				BufferedWriter outputSign = new BufferedWriter(outsign);
				Writer outdoc = new FileWriter(outfileDoc);
				BufferedWriter outputDoc = new BufferedWriter(outdoc);
				Writer outother = new FileWriter(outfileOther);
				BufferedWriter outputOther = new BufferedWriter(outother);
				Writer outsignWithJar = new FileWriter(outfileSignWithInnerJar);
				BufferedWriter outputSignWithJar = new BufferedWriter(outsignWithJar);
				Writer outJDTCore = new FileWriter(outfileJDTCore);
				BufferedWriter outputJDTCore = new BufferedWriter(outJDTCore);) {

			writeHeader(output);
			writeHeader(outputSign);
			writeHeader(outputSignWithJar);
			writeHeader(outputDoc);
			writeHeader(outputOther);
			writeHeader(outputJDTCore);
			count = 0;
			countSign = 0;
			countSignPlusInnerJar = 0;
			countDoc = 0;
			countOther = 0;
			countJDTCore = 0;
			String inputLine = "";

			while (inputLine != null) {
				inputLine = input.readLine();
				if (inputLine != null) {
					final Matcher matcher = mainPattern.matcher(inputLine);
					if (matcher.matches()) {

						final LogEntry newEntry = new LogEntry();
						newEntry.setName(matcher.group(1));
						// read and write differences, until next blank line
						do {
							inputLine = input.readLine();
							if ((inputLine != null) && (inputLine.length() > 0)) {
								newEntry.addReason(inputLine);
							}
						} while ((inputLine != null) && (inputLine.length() > 0));
						// //output.write(EOL);
						// now, do one more, to get the "info" that says
						// what was copied, or not.
						do {
							inputLine = input.readLine();
							if ((inputLine != null) && (inputLine.length() > 0)) {
								// except leave out the first line, which is a
								// long [INFO] line repeating what we already
								// know.
								if (!inputLine.startsWith("[INFO]")) {
									newEntry.addInfo(inputLine);
								}
							}
						} while ((inputLine != null) && (inputLine.length() > 0));
						// Write full log, for sanity check, if nothing else
						writeEntry(++count, output, newEntry);
						if (jdtCore(newEntry)) {
							writeEntry(++countJDTCore, outputJDTCore, newEntry);
						} else if (docItem(newEntry)) {
							writeEntry(++countDoc, outputDoc, newEntry);
						} else if (pureSignature(newEntry)) {
							writeEntry(++countSign, outputSign, newEntry);
						} else if (pureSignaturePlusInnerJar(newEntry)) {
							writeEntry(++countSignPlusInnerJar, outputSignWithJar, newEntry);
						} else {
							writeEntry(++countOther, outputOther, newEntry);
						}
					}
				}
			}
		}
	}

	private void writeHeader(final BufferedWriter output) throws IOException {
		output.write("Comparator differences from current build" + EOL);
		output.write("\t" + getBuildDirectory() + EOL);
		if (comparatorRepo != null) {
			output.write("compared to reference repo at " + EOL);
			output.write("\t" + getComparatorRepo() + EOL + EOL);
		}
	}

	private boolean jdtCore(final LogEntry newEntry) {
		boolean result = false;
		final String name = newEntry.getName();
		if (name.equals("eclipse.jdt.core/org.eclipse.jdt.core/pom.xml")) {
			result = true;
		}
		return result;
	}

	private boolean pureSignature(final LogEntry newEntry) {
		// if all lines match one of these critical patterns,
		// then assume "signature only" difference. If even
		// one of them does not match, assume not.
		boolean result = true;
		final List<String> reasons = newEntry.getReasons();
		for (final String reason : reasons) {
			final Matcher matcher1 = noclassifierPattern.matcher(reason);
			final Matcher matcher2 = classifier_sourcesPattern.matcher(reason);
			final Matcher matcher3 = classifier_sourcesfeaturePattern.matcher(reason);
			final Matcher matcher4 = sign1Pattern.matcher(reason);
			final Matcher matcher5 = sign2Pattern.matcher(reason);

			if (matcher1.matches() || matcher2.matches() || matcher3.matches() || matcher4.matches()
					|| matcher5.matches()) {
			} else {
				result = false;
				break;
			}
		}

		return result;
	}

	private boolean pureSignaturePlusInnerJar(final LogEntry newEntry) {
		// if all lines match one of these critical patterns,
		// then assume "signature only plus inner jar" difference. If even
		// one of them does not match, assume not.
		// TODO: refactor so less copy/paste of pureSignature method.
		boolean result = true;
		final List<String> reasons = newEntry.getReasons();
		for (final String reason : reasons) {
			final Matcher matcher1 = noclassifierPattern.matcher(reason);
			final Matcher matcher2 = classifier_sourcesPattern.matcher(reason);
			final Matcher matcher3 = classifier_sourcesfeaturePattern.matcher(reason);
			final Matcher matcher4 = sign1Pattern.matcher(reason);
			final Matcher matcher5 = sign2Pattern.matcher(reason);
			final Matcher matcher6 = jarPattern.matcher(reason);

			if (matcher1.matches() || matcher2.matches() || matcher3.matches() || matcher4.matches()
					|| matcher5.matches() || matcher6.matches()) {
			} else {
				result = false;
				break;
			}
		}

		return result;
	}

	public void setBuildDirectory(final String buildDirectory) {
		this.buildDirectory = buildDirectory;
	}

	private void writeEntry(int thistypeCount, final Writer output, final LogEntry newEntry) throws IOException {

		output.write(thistypeCount + ".  " + newEntry.getName() + EOL);
		final List<String> reasons = newEntry.getReasons();
		for (final String reason : reasons) {
			output.write(reason + EOL);
		}
		final List<String> infolist = newEntry.getInfo();
		for (final String info : infolist) {
			output.write(info + EOL);
		}
		output.write(EOL);
	}

	public String getComparatorRepo() {
		return comparatorRepo;
	}

	public void setComparatorRepo(String comparatorRepo) {
		this.comparatorRepo = comparatorRepo;
	}
}
