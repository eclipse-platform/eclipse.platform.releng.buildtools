/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
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
	public static final HashSet FILTERED_WARNINGS_IDS;

	public static final String FORBIDDEN_REFERENCE = "ForbiddenReference"; //$NON-NLS-1$
	public static final String DISCOURAGED_REFERENCE = "DiscouragedReference"; //$NON-NLS-1$

	static {
		FILTERED_WARNINGS_IDS = new HashSet();
		FILTERED_WARNINGS_IDS.add(FORBIDDEN_REFERENCE);
		FILTERED_WARNINGS_IDS.add(DISCOURAGED_REFERENCE);
	}

	protected Messages messages;


	private String convertToHTML(String s) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, max = s.length(); i < max; i++) {
			final char c = s.charAt(i);
			switch(c) {
				case '<' :
					buffer.append("&lt;"); //$NON-NLS-1$
					break;
				case '>' :
					buffer.append("&gt;"); //$NON-NLS-1$
					break;
				case '\"' :
					buffer.append("&quot;"); //$NON-NLS-1$
					break;
				case '&' :
					buffer.append("&amp;"); //$NON-NLS-1$
					break;
				case '^' :
					buffer.append("&and;"); //$NON-NLS-1$
					break;
				default:
					buffer.append(c);
			}
		}
		return String.valueOf(buffer);
	}

	public void dump(int formatVersion, Map options, LogDocumentNode documentNode) {
		switch(formatVersion) {
			case Converter.FORMAT_VERSION_2 :
				dumpVersion2(options, documentNode);
		}
	}

	private void dumpVersion2(Map options, LogDocumentNode documentNode) {
		String fileName = (String) options.get(Converter.OUTPUT_FILE_NAME);
		final ProblemSummaryNode summaryNode = documentNode.getSummaryNode();
		if (summaryNode == null ||
				(summaryNode.numberOfErrors == 0 && summaryNode.numberOfWarnings == 0)) return;
		try {
			Writer writer = new BufferedWriter(new FileWriter(fileName));
			final String pluginName = extractPluginName(fileName);
			if (pluginName == null) {
				writer.write(this.messages.getString("header")); //$NON-NLS-1$
			} else {
				String pattern = this.messages.getString("dom_header"); //$NON-NLS-1$
				writer.write(
					MessageFormat.format(pattern,
						new Object[] {
							pluginName,
							extractXmlFileName((String) options.get(Converter.INPUT_SOURCE))
						}
					));
			}
			ProblemSummaryNode problemSummaryNode = summaryNode;
			writeTopAnchor(writer);
			String pattern = this.messages.getString("problem.summary"); //$NON-NLS-1$
			writer.write(MessageFormat.format(pattern,
				new Object[] {
					Integer.toString(problemSummaryNode.numberOfProblems),
					Integer.toString(problemSummaryNode.numberOfErrors),
					Integer.toString(problemSummaryNode.numberOfWarnings)
				}));

			writeAnchorsReferences(writer);
			ProblemsNode[] problemsNodes = documentNode.getProblems();
			int globalErrorNumber = 1;

			writeErrorAnchor(writer);
			writeAnchorsReferencesErrors(writer);
			// dump errors
			for (int i = 0, max = problemsNodes.length; i < max; i++) {
				final ProblemsNode problemsNode = problemsNodes[i];
				ProblemNode[] problemNodes = problemsNode.getErrors();
				int length = problemNodes.length;
				if (length == 0) continue;
				pattern = this.messages.getString("errors.header"); //$NON-NLS-1$

				MessageFormat form = new MessageFormat(pattern);
				double[] warningsLimits = {1,2};
				String[] warningParts = {
						this.messages.getString("one_error"), //$NON-NLS-1$
						this.messages.getString("multiple_errors") //$NON-NLS-1$
				};
				ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
				final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
				form.setFormatByArgumentIndex(1, warningForm);
				Object[] arguments = new Object[] {
						sourceFileName,
						new Integer(problemsNode.numberOfErrors),
				};
				writer.write(form.format(arguments));
				for (int j = 0; j < length; j++) {
					ProblemNode problemNode = problemNodes[j];
					if ((j & 1) != 0) {
						pattern = this.messages.getString("errors.entry.odd"); //$NON-NLS-1$
					} else {
						pattern = this.messages.getString("errors.entry.even"); //$NON-NLS-1$
					}
					problemNode.setSources();
					writer.write(MessageFormat.format(pattern,
						new Object[] {
							sourceFileName,
							Integer.toString(globalErrorNumber),
							Integer.toString(j + 1),
							problemNode.id,
							Integer.toString(problemNode.line),
							convertToHTML(problemNode.message),
							convertToHTML(problemNode.sourceCodeBefore),
							convertToHTML(problemNode.sourceCode),
							convertToHTML(problemNode.sourceCodeAfter),
							getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
							Integer.toString(problemNode.charStart),
							Integer.toString(problemNode.charEnd),
						}));
					globalErrorNumber++;
				}
				writer.write(this.messages.getString("errors.footer")); //$NON-NLS-1$
			}

			writeOtherWarningsAnchor(writer);
			writeAnchorsReferencesOtherWarnings(writer);
			// dump other warnings
			for (int i = 0, max = problemsNodes.length; i < max; i++) {
				final ProblemsNode problemsNode = problemsNodes[i];
				ProblemNode[] problemNodes = problemsNode.getOtherWarnings();
				int length = problemNodes.length;
				if (length == 0) continue;

				pattern = this.messages.getString("other_warnings.header"); //$NON-NLS-1$
				MessageFormat form = new MessageFormat(pattern);
				double[] warningsLimits = {1,2};
				String[] warningParts = {
						this.messages.getString("one_warning"),//$NON-NLS-1$
						this.messages.getString("multiple_warnings")//$NON-NLS-1$
				};
				ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
				final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
				form.setFormatByArgumentIndex(1, warningForm);
				Object[] arguments = new Object[] {
						sourceFileName,
						new Integer(problemsNode.numberOfWarnings),
				};
				writer.write(form.format(arguments));
				for (int j = 0; j < length; j++) {
					ProblemNode problemNode = problemNodes[j];
					if ((j & 1) != 0) {
						pattern = this.messages.getString("warnings.entry.odd"); //$NON-NLS-1$
					} else {
						pattern = this.messages.getString("warnings.entry.even"); //$NON-NLS-1$
					}
					problemNode.setSources();
					writer.write(MessageFormat.format(pattern,
						new Object[] {
							sourceFileName,
							Integer.toString(globalErrorNumber),
							Integer.toString(j + 1),
							problemNode.id,
							Integer.toString(problemNode.line),
							convertToHTML(problemNode.message),
							convertToHTML(problemNode.sourceCodeBefore),
							convertToHTML(problemNode.sourceCode),
							convertToHTML(problemNode.sourceCodeAfter),
							getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
							Integer.toString(problemNode.charStart),
							Integer.toString(problemNode.charEnd),
						}));
					globalErrorNumber++;
				}
				writer.write(this.messages.getString("other_warnings.footer")); //$NON-NLS-1$
			}

			// dump forbidden accesses warnings
			writeForbiddenRulesWarningsAnchor(writer);
			writeAnchorsReferencesForbiddenRulesWarnings(writer);
			for (int i = 0, max = problemsNodes.length; i < max; i++) {
				final ProblemsNode problemsNode = problemsNodes[i];
				ProblemNode[] problemNodes = problemsNode.getForbiddenWarnings();
				int length = problemNodes.length;
				if (length == 0) continue;

				pattern = this.messages.getString("forbidden_warnings.header"); //$NON-NLS-1$
				MessageFormat form = new MessageFormat(pattern);
				double[] warningsLimits = {1,2};
				String[] warningParts = {
						this.messages.getString("one_warning"),//$NON-NLS-1$
						this.messages.getString("multiple_warnings")//$NON-NLS-1$
				};
				ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
				final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
				form.setFormatByArgumentIndex(1, warningForm);
				Object[] arguments = new Object[] {
						sourceFileName,
						new Integer(problemsNode.numberOfWarnings),
				};
				writer.write(form.format(arguments));
				for (int j = 0; j < length; j++) {
					ProblemNode problemNode = problemNodes[j];
					if ((j & 1) != 0) {
						pattern = this.messages.getString("warnings.entry.odd"); //$NON-NLS-1$
					} else {
						pattern = this.messages.getString("warnings.entry.even"); //$NON-NLS-1$
					}
					problemNode.setSources();
					writer.write(MessageFormat.format(pattern,
						new Object[] {
							sourceFileName,
							Integer.toString(globalErrorNumber),
							Integer.toString(j + 1),
							problemNode.id,
							Integer.toString(problemNode.line),
							convertToHTML(problemNode.message),
							convertToHTML(problemNode.sourceCodeBefore),
							convertToHTML(problemNode.sourceCode),
							convertToHTML(problemNode.sourceCodeAfter),
							getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
							Integer.toString(problemNode.charStart),
							Integer.toString(problemNode.charEnd),
						}));
					globalErrorNumber++;
				}
				writer.write(this.messages.getString("forbidden_warnings.footer")); //$NON-NLS-1$
			}

			// dump discouraged accesses warnings
			writeDiscouragedRulesWarningsAnchor(writer);
			writeAnchorsReferencesDiscouragedRulesWarnings(writer);
			for (int i = 0, max = problemsNodes.length; i < max; i++) {
				final ProblemsNode problemsNode = problemsNodes[i];
				ProblemNode[] problemNodes = problemsNode.getDiscouragedWarnings();
				int length = problemNodes.length;
				if (length == 0) continue;

				pattern = this.messages.getString("discouraged_warnings.header"); //$NON-NLS-1$
				MessageFormat form = new MessageFormat(pattern);
				double[] warningsLimits = {1,2};
				String[] warningParts = {
						this.messages.getString("one_warning"),//$NON-NLS-1$
						this.messages.getString("multiple_warnings")//$NON-NLS-1$
				};
				ChoiceFormat warningForm = new ChoiceFormat(warningsLimits, warningParts);
				final String sourceFileName = extractRelativePath(problemsNode.sourceFileName, pluginName);
				form.setFormatByArgumentIndex(1, warningForm);
				Object[] arguments = new Object[] {
						sourceFileName,
						new Integer(problemsNode.numberOfWarnings),
				};
				writer.write(form.format(arguments));
				for (int j = 0; j < length; j++) {
					ProblemNode problemNode = problemNodes[j];
					if ((j & 1) != 0) {
						pattern = this.messages.getString("warnings.entry.odd"); //$NON-NLS-1$
					} else {
						pattern = this.messages.getString("warnings.entry.even"); //$NON-NLS-1$
					}
					problemNode.setSources();
					writer.write(MessageFormat.format(pattern,
						new Object[] {
							sourceFileName,
							Integer.toString(globalErrorNumber),
							Integer.toString(j + 1),
							problemNode.id,
							Integer.toString(problemNode.line),
							convertToHTML(problemNode.message),
							convertToHTML(problemNode.sourceCodeBefore),
							convertToHTML(problemNode.sourceCode),
							convertToHTML(problemNode.sourceCodeAfter),
							getUnderLine(problemNode.sourceCodeBefore, problemNode.sourceCodeAfter),
							Integer.toString(problemNode.charStart),
							Integer.toString(problemNode.charEnd),
						}));
					globalErrorNumber++;
				}
				writer.write(this.messages.getString("discouraged_warnings.footer")); //$NON-NLS-1$
			}

			writer.write(this.messages.getString("footer")); //$NON-NLS-1$
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		}
	}

	private String extractPluginName(String fileName) {
		// fileName is fully qualified and we want to extract the segment before the log file name
		// file name contains only '/'
		String logName = fileName.replace('\\', '/');
		int index = logName.lastIndexOf('/');
		if (index == -1) return null;
		int index2 = logName.lastIndexOf('/', index - 1);
		if (index2 == -1) return null;
		return logName.substring(index2 + 1, index);
	}

	private String extractRelativePath(String sourceFileName, String pluginName) {
		if (pluginName == null) return sourceFileName;
		int index = pluginName.indexOf('_');
		if (index == -1) return sourceFileName;
		String pluginShortName = pluginName.substring(0, index);
		int index2 = sourceFileName.indexOf(pluginShortName);
		if (index2 == -1) return sourceFileName;
		return sourceFileName.substring(index2 + pluginShortName.length(), sourceFileName.length());
	}

	private String extractXmlFileName(String fileName) {
		// fileName is fully qualified and we want to extract the segment before the log file name
		// file name contains only '/'
		String logName = fileName.replace('\\', '/');
		int index = logName.lastIndexOf('/');
		if (index == -1) return null;
		return logName.substring(index + 1, logName.length());
	}
}
