/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.dom;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ChoiceFormat;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.releng.build.tools.convert.ant.Converter;
import org.eclipse.releng.build.tools.convert.ant.Messages;

public abstract class AbstractDOMConverter implements IDOMConverter {

    public static final HashSet<String> FILTERED_WARNINGS_IDS;

    public static final String  FORBIDDEN_REFERENCE   = "ForbiddenReference";  //$NON-NLS-1$
    public static final String  DISCOURAGED_REFERENCE = "DiscouragedReference"; //$NON-NLS-1$

    static {
        FILTERED_WARNINGS_IDS = new HashSet<>();
        FILTERED_WARNINGS_IDS.add(FORBIDDEN_REFERENCE);
        FILTERED_WARNINGS_IDS.add(DISCOURAGED_REFERENCE);
    }

    protected Messages          messages;

    private String convertToHTML(final String s) {
        final StringBuffer buffer = new StringBuffer();
        for (int i = 0, max = s.length(); i < max; i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '<':
                    buffer.append("&lt;"); //$NON-NLS-1$
                    break;
                case '>':
                    buffer.append("&gt;"); //$NON-NLS-1$
                    break;
                case '\"':
                    buffer.append("&quot;"); //$NON-NLS-1$
                    break;
                case '&':
                    buffer.append("&amp;"); //$NON-NLS-1$
                    break;
                case '^':
                    buffer.append("&and;"); //$NON-NLS-1$
                    break;
                default:
                    buffer.append(c);
            }
        }
        return String.valueOf(buffer);
    }

    @Override
    public void dump(final int formatVersion, final Map<String, String> options, final LogDocumentNode documentNode) {
        switch (formatVersion) {
            case Converter.FORMAT_VERSION_2:
                dumpVersion2(options, documentNode);
        }
    }

    private void dumpVersion2(final Map<String, String> options, final LogDocumentNode documentNode) {
        final String fileName = options.get(Converter.OUTPUT_FILE_NAME);
        final ProblemSummaryNode summaryNode = documentNode.getSummaryNode();
        if ((summaryNode == null) || (summaryNode.numberOfProblems == 0)) {
            return;
        }
        try (final Writer writer = new BufferedWriter(new FileWriter(fileName))){
            final String pluginName = extractPluginName(fileName);
            if (pluginName == null) {
                writer.write(messages.getString("header")); //$NON-NLS-1$
            } else {
                final String pattern = messages.getString("dom_header"); //$NON-NLS-1$
                writer.write(MessageFormat.format(pattern,
                        new Object[] { pluginName, extractXmlFileName(options.get(Converter.INPUT_SOURCE)) }));
            }
            final ProblemSummaryNode problemSummaryNode = summaryNode;
            writeTopAnchor(writer);
            String pattern = messages.getString("problem.summary"); //$NON-NLS-1$
            writer.write(MessageFormat.format(pattern, new Object[] { Integer.toString(problemSummaryNode.numberOfProblems),
                    Integer.toString(problemSummaryNode.numberOfErrors), Integer.toString(problemSummaryNode.numberOfWarnings), 
                    Integer.toString(problemSummaryNode.numberOfInfos)}));

            writeAnchorsReferences(writer);
            final ProblemsNode[] problemsNodes = documentNode.getProblems();
            int globalErrorNumber = 1;

            writeErrorAnchor(writer);
            writeAnchorsReferencesErrors(writer);
            // dump errors
            for (int i = 0, max = problemsNodes.length; i < max; i++) {
                final ProblemsNode problemsNode = problemsNodes[i];
                final ProblemNode[] problemNodes = problemsNode.getErrors();
                final int length = problemNodes.length;
                if (length == 0) {
                    continue;
                }
                pattern = messages.getString("errors.header"); //$NON-NLS-1$

                final MessageFormat form = new MessageFormat(pattern);
                final double[] warningsLimits = { 1, 2 };
                final String[] warningParts = { messages.getString("one_error"), //$NON-NLS-1$
                        messages.getString("multiple_errors") //$NON-NLS-1$
                };
                final ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
                final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
                form.setFormatByArgumentIndex(1, warningForm);
                final Object[] arguments = new Object[] { sourceFileName, new Integer(problemsNode.numberOfErrors), };
                writer.write(form.format(arguments));
                for (int j = 0; j < length; j++) {
                    final ProblemNode problemNode = problemNodes[j];
                    if ((j & 1) != 0) {
                        pattern = messages.getString("errors.entry.odd"); //$NON-NLS-1$
                    } else {
                        pattern = messages.getString("errors.entry.even"); //$NON-NLS-1$
                    }
                    problemNode.setSources();
                    writer.write(MessageFormat.format(
                            pattern,
                            new Object[] { sourceFileName, Integer.toString(globalErrorNumber), Integer.toString(j + 1),
                                    problemNode.id, Integer.toString(problemNode.line), convertToHTML(problemNode.message),
                                    convertToHTML(problemNode.sourceCodeBefore), convertToHTML(problemNode.sourceCode),
                                    convertToHTML(problemNode.sourceCodeAfter),
                                    getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
                                    Integer.toString(problemNode.charStart), Integer.toString(problemNode.charEnd), }));
                    globalErrorNumber++;
                }
                writer.write(messages.getString("errors.footer")); //$NON-NLS-1$
            }

            writeOtherWarningsAnchor(writer);
            writeAnchorsReferencesOtherWarnings(writer);
            // dump other warnings
            for (int i = 0, max = problemsNodes.length; i < max; i++) {
                final ProblemsNode problemsNode = problemsNodes[i];
                final ProblemNode[] problemNodes = problemsNode.getOtherWarnings();
                final int length = problemNodes.length;
                if (length == 0) {
                    continue;
                }

                pattern = messages.getString("other_warnings.header"); //$NON-NLS-1$
                final MessageFormat form = new MessageFormat(pattern);
                final double[] warningsLimits = { 1, 2 };
                final String[] warningParts = { messages.getString("one_warning"),//$NON-NLS-1$
                        messages.getString("multiple_warnings") //$NON-NLS-1$
                };
                final ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
                final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
                form.setFormatByArgumentIndex(1, warningForm);
                final Object[] arguments = new Object[] { sourceFileName, new Integer(problemsNode.numberOfWarnings), };
                writer.write(form.format(arguments));
                for (int j = 0; j < length; j++) {
                    final ProblemNode problemNode = problemNodes[j];
                    if ((j & 1) != 0) {
                        pattern = messages.getString("warnings.entry.odd"); //$NON-NLS-1$
                    } else {
                        pattern = messages.getString("warnings.entry.even"); //$NON-NLS-1$
                    }
                    problemNode.setSources();
                    writer.write(MessageFormat.format(
                            pattern,
                            new Object[] { sourceFileName, Integer.toString(globalErrorNumber), Integer.toString(j + 1),
                                    problemNode.id, Integer.toString(problemNode.line), convertToHTML(problemNode.message),
                                    convertToHTML(problemNode.sourceCodeBefore), convertToHTML(problemNode.sourceCode),
                                    convertToHTML(problemNode.sourceCodeAfter),
                                    getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
                                    Integer.toString(problemNode.charStart), Integer.toString(problemNode.charEnd), }));
                    globalErrorNumber++;
                }
                writer.write(messages.getString("other_warnings.footer")); //$NON-NLS-1$
            }

            // dump infos
            writeInfosAnchor(writer);
            writeAnchorsReferencesInfos(writer);
            for (int i = 0, max = problemsNodes.length; i < max; i++) {
                final ProblemsNode problemsNode = problemsNodes[i];
                final ProblemNode[] problemNodes = problemsNode.getInfos();
                final int length = problemNodes.length;
                if (length == 0) {
                    continue;
                }

                pattern = messages.getString("infos.header"); //$NON-NLS-1$
                final MessageFormat form = new MessageFormat(pattern);
                final double[] warningsLimits = { 1, 2 };
                final String[] warningParts = { messages.getString("one_info"),//$NON-NLS-1$
                        messages.getString("multiple_infos") //$NON-NLS-1$
                };
                final ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
                final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
                form.setFormatByArgumentIndex(1, warningForm);
                final Object[] arguments = new Object[] { sourceFileName, new Integer(problemsNode.numberOfInfos), };
                writer.write(form.format(arguments));
                for (int j = 0; j < length; j++) {
                    final ProblemNode problemNode = problemNodes[j];
                    if ((j & 1) != 0) {
                        pattern = messages.getString("infos.entry.odd"); //$NON-NLS-1$
                    } else {
                        pattern = messages.getString("infos.entry.even"); //$NON-NLS-1$
                    }
                    problemNode.setSources();
                    writer.write(MessageFormat.format(
                            pattern,
                            new Object[] { sourceFileName, Integer.toString(globalErrorNumber), Integer.toString(j + 1),
                                    problemNode.id, Integer.toString(problemNode.line), convertToHTML(problemNode.message),
                                    convertToHTML(problemNode.sourceCodeBefore), convertToHTML(problemNode.sourceCode),
                                    convertToHTML(problemNode.sourceCodeAfter),
                                    getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
                                    Integer.toString(problemNode.charStart), Integer.toString(problemNode.charEnd), }));
                    globalErrorNumber++;
                }
                writer.write(messages.getString("infos.footer")); //$NON-NLS-1$
            }

            // dump forbidden accesses warnings
            writeForbiddenRulesWarningsAnchor(writer);
            writeAnchorsReferencesForbiddenRulesWarnings(writer);
            for (int i = 0, max = problemsNodes.length; i < max; i++) {
                final ProblemsNode problemsNode = problemsNodes[i];
                final ProblemNode[] problemNodes = problemsNode.getForbiddenWarnings();
                final int length = problemNodes.length;
                if (length == 0) {
                    continue;
                }

                pattern = messages.getString("forbidden_warnings.header"); //$NON-NLS-1$
                final MessageFormat form = new MessageFormat(pattern);
                final double[] warningsLimits = { 1, 2 };
                final String[] warningParts = { messages.getString("one_warning"),//$NON-NLS-1$
                        messages.getString("multiple_warnings") //$NON-NLS-1$
                };
                final ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
                final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
                form.setFormatByArgumentIndex(1, warningForm);
                final Object[] arguments = new Object[] { sourceFileName, new Integer(problemsNode.numberOfWarnings), };
                writer.write(form.format(arguments));
                for (int j = 0; j < length; j++) {
                    final ProblemNode problemNode = problemNodes[j];
                    if ((j & 1) != 0) {
                        pattern = messages.getString("warnings.entry.odd"); //$NON-NLS-1$
                    } else {
                        pattern = messages.getString("warnings.entry.even"); //$NON-NLS-1$
                    }
                    problemNode.setSources();
                    writer.write(MessageFormat.format(
                            pattern,
                            new Object[] { sourceFileName, Integer.toString(globalErrorNumber), Integer.toString(j + 1),
                                    problemNode.id, Integer.toString(problemNode.line), convertToHTML(problemNode.message),
                                    convertToHTML(problemNode.sourceCodeBefore), convertToHTML(problemNode.sourceCode),
                                    convertToHTML(problemNode.sourceCodeAfter),
                                    getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
                                    Integer.toString(problemNode.charStart), Integer.toString(problemNode.charEnd), }));
                    globalErrorNumber++;
                }
                writer.write(messages.getString("forbidden_warnings.footer")); //$NON-NLS-1$
            }

            // dump discouraged accesses warnings
            writeDiscouragedRulesWarningsAnchor(writer);
            writeAnchorsReferencesDiscouragedRulesWarnings(writer);
            for (int i = 0, max = problemsNodes.length; i < max; i++) {
                final ProblemsNode problemsNode = problemsNodes[i];
                final ProblemNode[] problemNodes = problemsNode.getDiscouragedWarnings();
                final int length = problemNodes.length;
                if (length == 0) {
                    continue;
                }

                pattern = messages.getString("discouraged_warnings.header"); //$NON-NLS-1$
                final MessageFormat form = new MessageFormat(pattern);
                final double[] warningsLimits = { 1, 2 };
                final String[] warningParts = { messages.getString("one_warning"),//$NON-NLS-1$
                        messages.getString("multiple_warnings") //$NON-NLS-1$
                };
                final ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
                final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
                form.setFormatByArgumentIndex(1, warningForm);
                final Object[] arguments = new Object[] { sourceFileName, new Integer(problemsNode.numberOfWarnings), };
                writer.write(form.format(arguments));
                for (int j = 0; j < length; j++) {
                    final ProblemNode problemNode = problemNodes[j];
                    if ((j & 1) != 0) {
                        pattern = messages.getString("warnings.entry.odd"); //$NON-NLS-1$
                    } else {
                        pattern = messages.getString("warnings.entry.even"); //$NON-NLS-1$
                    }
                    problemNode.setSources();
                    writer.write(MessageFormat.format(
                            pattern,
                            new Object[] { sourceFileName, Integer.toString(globalErrorNumber), Integer.toString(j + 1),
                                    problemNode.id, Integer.toString(problemNode.line), convertToHTML(problemNode.message),
                                    convertToHTML(problemNode.sourceCodeBefore), convertToHTML(problemNode.sourceCode),
                                    convertToHTML(problemNode.sourceCodeAfter),
                                    getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
                                    Integer.toString(problemNode.charStart), Integer.toString(problemNode.charEnd), }));
                    globalErrorNumber++;
                }
                writer.write(messages.getString("discouraged_warnings.footer")); //$NON-NLS-1$
            }

            writer.write(messages.getString("footer")); //$NON-NLS-1$
            writer.flush();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private String extractPluginName(final String fileName) {
        // fileName is fully qualified and we want to extract the segment before
        // the log file name
        // file name contains only '/'
        final String logName = fileName.replace('\\', '/');
        final int index = logName.lastIndexOf('/');
        if (index == -1) {
            return null;
        }
        final int index2 = logName.lastIndexOf('/', index - 1);
        if (index2 == -1) {
            return null;
        }
        return logName.substring(index2 + 1, index);
    }

    private String extractRelativePath(final String sourceFileName, final String pluginName) {
        if (pluginName == null) {
            return sourceFileName;
        }
        final int index = pluginName.indexOf('_');
        if (index == -1) {
            return sourceFileName;
        }
        final String pluginShortName = pluginName.substring(0, index);
        final int index2 = sourceFileName.indexOf(pluginShortName);
        if (index2 == -1) {
            return sourceFileName;
        }
        return sourceFileName.substring(index2 + pluginShortName.length(), sourceFileName.length());
    }

    private String extractXmlFileName(final String fileName) {
        // fileName is fully qualified and we want to extract the segment before
        // the log file name
        // file name contains only '/'
        final String logName = fileName.replace('\\', '/');
        final int index = logName.lastIndexOf('/');
        if (index == -1) {
            return null;
        }
        return logName.substring(index + 1, logName.length());
    }
}
