/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.test.internal.performance.results.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.eclipse.test.internal.performance.results.db.BuildResults;
import org.eclipse.test.internal.performance.results.db.DB_Results;

/**
 * Utility methods for statistics. Got from org.eclipse.test.performance
 * framework
 */
public final class Util implements IPerformancesConstants {

    // Percentages
    public static final NumberFormat PERCENTAGE_FORMAT = NumberFormat.getPercentInstance(Locale.US);
    static {
        PERCENTAGE_FORMAT.setMaximumFractionDigits(2);
    }
    public static final NumberFormat DOUBLE_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    static {
        DOUBLE_FORMAT.setMaximumFractionDigits(2);
    }

    // Strings
    public static final String LINE_SEPARATOR     = System.lineSeparator();

    // Build prefixes
    public static final List<String>   ALL_BUILD_PREFIXES = new ArrayList<>(3);
    static {
        ALL_BUILD_PREFIXES.add("I");
        ALL_BUILD_PREFIXES.add("N");
        ALL_BUILD_PREFIXES.add("M");
    }
    public static final List<String> BUILD_PREFIXES = new ArrayList<>(2);
    static {
        BUILD_PREFIXES.add("I");
        BUILD_PREFIXES.add("N");
    }
    public static final List<String> MAINTENANCE_BUILD_PREFIXES = new ArrayList<>(2);
    static {
        MAINTENANCE_BUILD_PREFIXES.add("I");
        MAINTENANCE_BUILD_PREFIXES.add("M");
    }
    public static final List<String> BASELINE_BUILD_PREFIXES = new ArrayList<>(1);
    static {
        BASELINE_BUILD_PREFIXES.add(DB_Results.getDbBaselinePrefix());
    }

    public static final BuildDateComparator BUILD_DATE_COMPARATOR = new BuildDateComparator();

    // Components constants
    public static final String              ORG_ECLIPSE           = "org.eclipse.";

    static class BuildDateComparator implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {
            String buildDate1 = getBuildDate(s1);
            String buildDate2 = getBuildDate(s2);
		// Not the greatest sanity check, found some cases, when "getting started"
		// where buildname was "4.1.1" or "${buildid}, which would cause a NPE.
            // So, simple attempt to provide a more helpful message.
            if (buildDate1 == null) {
                throw new IllegalArgumentException("Buildname did not have a date, as expected: " + s1);
            }
            if (buildDate2 == null) {
                throw new IllegalArgumentException("Buildname did not have a date, as expected: " + s2);
            }
            return buildDate1.compareTo(buildDate2);
        }
    }

    // Static information for time and date
    public static final int              ONE_MINUTE  = 60000;
    public static final long             ONE_HOUR    = 3600000L;
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmm"); //$NON-NLS-1$

    /**
     * Return the name to display for the given component.
     * <p>
 * This name is built from the name of the component selected in the Components view.
 * The rules to build the name are:
     * <ol>
 * <li>If the component name does not start with "org.eclipse" then the part name is just
 * 		"<b>component name</b>"</li>
     * <li>Otherwise, remove "org.eclipse." form the component name and count
     * the tokens separated by a dot ('.')
     * <ul>
 * 		<li>If there's only one remaining token, then the part name is "<b>Platform/</b>"
 * 			followed by:
     * <ul>
     * <li>"<b><i>TOKEN</i></b>" if token is less than 3 characters,</li>
     * <li>"<b><i>Token</i></b>" otherwise</li>
     * </ul>
     * </li>
 * 		<li>Otherwise then the part name is "<b><i>FIRST_TOKEN</i></b>" followed by:
     * <ul>
     * <li>for each followed additional token:
     * <ul>
     * <li>"<b><i>TOKEN</i></b>" if token is less than 3 characters,</li>
     * <li>"<b><i>Token</i></b>" otherwise</li>
     * </ul>
     * </li>
     * </ul>
     * </li>
     * </ul>
     * </ol>
     * E.g.
     * <ul>
     * 	<li>org.eclipse.ui -&gt; "Platform/UI"</li>
     * 	<li>org.eclipse.swt -&gt; "Platform/SWT"</li>
     * 	<li>org.eclipse.team -&gt; "Platform/Team"</li>
     * 	<li>org.eclipse.jdt.ui -&gt; "JDT/UI"</li>
     * 	<li>org.eclipse.jdt.core -&gt; "JDT/Core"</li>
     * 	<li>org.eclipse.pde.api.tools -&gt; "PDE/API Tools"</li>
     * </ul>
     */
    public static String componentDisplayName(String componentName) {
        String partName;
        StringBuilder buffer = null;
        if (componentName.startsWith(ORG_ECLIPSE)) {
            partName = componentName.substring(ORG_ECLIPSE.length());
            StringTokenizer tokenizer = new StringTokenizer(partName, ".");
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (buffer == null) {
                    if (tokenizer.hasMoreTokens()) {
                        buffer = new StringBuilder("'" + token.toUpperCase());
                        buffer.append('/');
                    } else {
                        buffer = new StringBuilder("'Platform/");
                        if (token.length() > 3) {
                            buffer.append(Character.toUpperCase(token.charAt(0)));
                            buffer.append(token.substring(1));
                        } else {
                            buffer.append(token.toUpperCase());
                        }
                    }
                } else {
                    if (token.length() > 3) {
                        buffer.append(Character.toUpperCase(token.charAt(0)));
                        buffer.append(token.substring(1));
                    } else {
                        buffer.append(token.toUpperCase());
                    }
				if (tokenizer.hasMoreTokens()) buffer.append(' ');
                }
            }
        } else {
            buffer = new StringBuilder("'");
            buffer.append(componentName);
            buffer.append("'");
        }
        if (buffer == null) {
            throw new RuntimeException("buffer was unexpectedly null. Program error?");
        }
        buffer.append("' results");
        return buffer.toString();
    }

    /**
     * Compute the student t-test values.
     *
     * @see "http://en.wikipedia.org/wiki/Student's_t-test"
     *
 * @param baselineResults The baseline build
 * @param buildResults The current build
     * @return The student t-test value as a double.
     */
    public static double computeTTest(BuildResults baselineResults, BuildResults buildResults) {

        double ref = baselineResults.getValue();
        double val = buildResults.getValue();

        double delta = ref - val;
        long dfRef = baselineResults.getCount() - 1;
        double sdRef = baselineResults.getDeviation();
        long dfVal = buildResults.getCount() - 1;
        double sdVal = buildResults.getDeviation();
        // TODO if the stdev's are not sufficiently similar, we have to take a
        // different approach

        if (!Double.isNaN(sdRef) && !Double.isNaN(sdVal) && dfRef > 0 && dfVal > 0) {
            long df = dfRef + dfVal;
            double sp_square = (dfRef * sdRef * sdRef + dfVal * sdVal * sdVal) / df;

            double se_diff = Math.sqrt(sp_square * (1.0 / (dfRef + 1) + 1.0 / (dfVal + 1)));
            double t = Math.abs(delta / se_diff);
            return t;
        }

        return -1;
    }

    /**
     * Copy a file to another location.
     *
 * @param src the source file.
 * @param dest the destination.
     * @return <code>true</code> if the file was successfully copied,
     *         <code>false</code> otherwise.
     */
    public static boolean copyFile(File src, File dest) {

        try (InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest)){

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Copy a file content to another location.
     *
 * @param in the input stream.
 * @param dest the destination.
     * @return <code>true</code> if the file was successfully copied,
     *         <code>false</code> otherwise.
     */
    public static boolean copyStream(InputStream in, File dest) {

        try (OutputStream out = new FileOutputStream(dest)){

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Return the build date as yyyyMMddHHmm.
     *
 * @param buildName The build name (e.g. I20090806-0100)
     * @return The date as a string.
     */
    public static String getBuildDate(String buildName) {
        return getBuildDate(buildName, DB_Results.getDbBaselinePrefix());
    }

    /**
     * Return the build date as yyyyMMddHHmm.
     *
 * @param buildName The build name (e.g. I20090806-0100)
 * @param baselinePrefix The baseline prefix (e.g. {@link DB_Results#getDbBaselinePrefix()})
     * @return The date as a string.
     */
    public static String getBuildDate(String buildName, String baselinePrefix) {

        String result = "";
        // Baseline name
        if (baselinePrefix != null && buildName.startsWith(baselinePrefix)) {
            int length = buildName.length();
            result = buildName.substring(length - 12, length);
        } else {
            // Build name
            char first = buildName.charAt(0);
            // TODO: should check if in buildIdPrefixes instead of hard coding
            if (first == 'N' || first == 'I' || first == 'M') {
                result = buildName.substring(1, 9) + buildName.substring(10, 14);
            } else {
                //System.err.println("\n\tERROR: buildName did not have expected format. buildName: " + buildName);
                // 2015/10/30 (dw) following too complicated (and, unsure if correct, so should be removed or simplified greatly bullet proofed)
                // Try with date format, to handle old release baselines
                int length = buildName.length() - 12 /* length of date */;
                for (int i = 0; i <= length; i++) {
                    try {
                        String substring = i == 0 ? buildName : buildName.substring(i);
                        Util.DATE_FORMAT.parse(substring);
                        // if no exception,
                        result = substring;
                    }
                    catch (ParseException ex) {
                        result = null;
                    }
              }
            }
        }
        return result;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static boolean matchPattern(String name, String pattern) {
        if (pattern.equals("*")) //$NON-NLS-1$
            return true;
        if (pattern.indexOf('*') < 0 && pattern.indexOf('?') < 0) {
            pattern += "*"; //$NON-NLS-1$
        }
        StringTokenizer tokenizer = new StringTokenizer(pattern, "*?", true); //$NON-NLS-1$
        int start = 0;
        String previous = ""; //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (!token.equals("*") && !token.equals("?")) { //$NON-NLS-1$ //$NON-NLS-2$
                if (previous.equals("*")) { //$NON-NLS-1$
                    int idx = name.substring(start).indexOf(token);
                    if (idx < 0)
                        return false;
                    start += idx;
                } else {
                    if (previous.equals("?")) //$NON-NLS-1$
                        start++;
                    if (!name.substring(start).startsWith(token))
                        return false;
                }
                start += token.length();
            }
            previous = token;
        }
        if (previous.equals("*")) { //$NON-NLS-1$
            return true;
        } else if (previous.equals("?")) { //$NON-NLS-1$
            return name.length() == start;
        }
        return name.endsWith(previous);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static double round(double value) {
        return Math.round(value * 10000) / 10000.0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static double round(double value, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("Should have a precision at least greater than 0!");
        }
        if (precision == 0)
            return (long) Math.floor(value);
        double factor = 10;
        int n = 1;
        while (n++ < precision)
            factor *= 10;
        return Math.round(value * factor) / factor;
    }

    /**
     * Returns a string to display the given time as a duration formatted as
     * "hh:mm:ss".
     *
     * @param time
     *            The time to format as a long.
     * @return The formatted string.
     */
    public static String timeChrono(long time) {
        if (time < 1000) { // less than 1s
            return "00:00:00"; //$NON-NLS-1$
        }
        StringBuilder buffer = new StringBuilder();
        int seconds = (int) (time / 1000);
        if (seconds < 60) {
            buffer.append("00:00:"); //$NON-NLS-1$
            if (seconds < 10)
                buffer.append('0');
            buffer.append(seconds);
        } else {
            int minutes = seconds / 60;
            if (minutes < 60) {
                buffer.append("00:"); //$NON-NLS-1$
                if (minutes < 10)
                    buffer.append('0');
                buffer.append(minutes);
                buffer.append(':');
                seconds = seconds % 60;
                if (seconds < 10)
                    buffer.append('0');
                buffer.append(seconds);
            } else {
                int hours = minutes / 60;
                if (hours < 10)
                    buffer.append('0');
                buffer.append(hours);
                buffer.append(':');
                minutes = minutes % 60;
                if (minutes < 10)
                    buffer.append('0');
                buffer.append(minutes);
                buffer.append(':');
                seconds = seconds % 60;
                if (seconds < 10)
                    buffer.append('0');
                buffer.append(seconds);
            }
        }
        return buffer.toString();
    }

    /**
     * Returns a string to display the given time as the hour of the day
     * formatted as "hh:mm:ss".
     *
     * @param time
     *            The time to format as a long.
     * @return The formatted string.
     */
    public static String timeEnd(long time) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.SECOND, (int) (time / 1000));
        Date date = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("KK:mm:ss"); //$NON-NLS-1$
        return dateFormat.format(date);
    }

    /**
     * Returns a string to display the given time as a duration formatted as:
     * <ul>
     * <li>"XXXms" if the duration is less than 0.1s (e.g. "543ms")</li>
     * <li>"X.YYs" if the duration is less than 1s (e.g. "5.43s")</li>
     * <li>"XX.Ys" if the duration is less than 1mn (e.g. "54.3s")</li>
     * <li>"XXmn XXs" if the duration is less than 1h (e.g. "54mn 3s")</li>
     * <li>"XXh XXmn XXs" if the duration is over than 1h (e.g. "5h 4mn 3s")
     * </li>
     * </ul>
     *
     * @param time
     *            The time to format as a long.
     * @return The formatted string.
     */
    public static String timeString(long time) {
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(1);
        StringBuilder buffer = new StringBuilder();
        if (time == 0) {
            // print nothing
        }
        if (time < 100) { // less than 0.1s
            buffer.append(time);
            buffer.append("ms"); //$NON-NLS-1$
        } else if (time < 1000) { // less than 1s
            if ((time % 100) != 0) {
                format.setMaximumFractionDigits(2);
            }
            buffer.append(format.format(time / 1000.0));
            buffer.append("s"); //$NON-NLS-1$
        } else if (time < Util.ONE_MINUTE) { // less than 1mn
            if ((time % 1000) == 0) {
                buffer.append(time / 1000);
            } else {
                buffer.append(format.format(time / 1000.0));
            }
            buffer.append("s"); //$NON-NLS-1$
        } else if (time < Util.ONE_HOUR) { // less than 1h
            buffer.append(time / Util.ONE_MINUTE).append("mn "); //$NON-NLS-1$
            long seconds = time % Util.ONE_MINUTE;
            buffer.append(seconds / 1000);
            buffer.append("s"); //$NON-NLS-1$
        } else { // more than 1h
            long h = time / Util.ONE_HOUR;
            buffer.append(h).append("h "); //$NON-NLS-1$
            long m = (time % Util.ONE_HOUR) / Util.ONE_MINUTE;
            buffer.append(m).append("mn "); //$NON-NLS-1$
            long seconds = m % Util.ONE_MINUTE;
            buffer.append(seconds / 1000);
            buffer.append("s"); //$NON-NLS-1$
        }
        return buffer.toString();
    }

    private Util() {
        // don't instantiate
    }

}
