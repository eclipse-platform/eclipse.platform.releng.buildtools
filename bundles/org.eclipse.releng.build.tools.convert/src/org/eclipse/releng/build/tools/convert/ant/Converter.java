/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng.build.tools.convert.ant;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.releng.build.tools.convert.dom.AbstractDOMConverter;
import org.eclipse.releng.build.tools.convert.dom.IDOMConverter;
import org.eclipse.releng.build.tools.convert.dom.LogDocumentNode;
import org.eclipse.releng.build.tools.convert.dom.ProblemNode;
import org.eclipse.releng.build.tools.convert.dom.ProblemSummaryNode;
import org.eclipse.releng.build.tools.convert.dom.ProblemsNode;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Converter {
	private static final String HTML_EXTENSION = ".html"; //$NON-NLS-1$

	private static final String TXT_EXTENSION = ".txt"; //$NON-NLS-1$

	private static final String XML_EXTENSION = ".xml"; //$NON-NLS-1$

	public static final String INPUT_SOURCE = "inputSource";//$NON-NLS-1$

	public static final String CONVERTER_ID = "converterID";//$NON-NLS-1$

	public static final String OUTPUT_FILE_NAME = "outputFileName";//$NON-NLS-1$

	public static final String ENABLE_VALIDATION = "enableValidation";//$NON-NLS-1$

	public static final String RECURSIVE = "recurse";//$NON-NLS-1$

	public static final int FORMAT_VERSION_2 = 2;

	public static final int CURRENT_FORMAT_VERSION = FORMAT_VERSION_2;

	private static final FileFilter XML_FILTER = new FileFilter() {
		/*
		 * (non-Javadoc)
		 *
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		public boolean accept(File pathname) {
			String path = pathname.getAbsolutePath().toLowerCase();
			return path.endsWith(XML_EXTENSION) || pathname.isDirectory();
		}
	};

	private HashMap options;

	public static File[] getAllFiles(File root, FileFilter fileFilter) {
		ArrayList files = new ArrayList();
		if (root.isDirectory()) {
			collectAllFiles(root, files, fileFilter);
			File[] result = new File[files.size()];
			files.toArray(result);
			return result;
		}
		return null;
	}

	private static void collectAllFiles(File root, ArrayList collector, FileFilter fileFilter) {
		File[] files = root.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			final File currentFile = files[i];
			if (currentFile.isDirectory()) {
				collectAllFiles(currentFile, collector, fileFilter);
			} else {
				collector.add(currentFile);
			}
		}
	}

	public Converter() {
		this.options = new HashMap();
	}

	public void configure(String[] args) {
		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("Arguments cannot be empty");//$NON-NLS-1$
		}

		// set default options
		this.options.put(ENABLE_VALIDATION, "false"); //$NON-NLS-1$
		int converterID = ConverterFactory.HTML;

		int argCount = args.length;
		int index = 0;
		boolean setOutputFile = false;
		boolean setValidation = false;
		boolean setInputFile = false;

		final int DEFAULT_MODE = 0;
		final int OUTPUT_FILE_MODE = 1;
		final int INPUT_FILE_MODE = 2;

		int mode = DEFAULT_MODE;

		loop: while (index < argCount) {
			String currentArg = args[index++];

			switch (mode) {
				case INPUT_FILE_MODE:
					this.options.put(INPUT_SOURCE, currentArg);
					mode = DEFAULT_MODE;
					break;
				case OUTPUT_FILE_MODE:
					if (currentArg.toLowerCase().endsWith(TXT_EXTENSION)) {
						converterID = ConverterFactory.TXT;
					}
					this.options.put(OUTPUT_FILE_NAME, currentArg);
					mode = DEFAULT_MODE;
					continue loop;
				case DEFAULT_MODE:
					if (currentArg.equals("-v")) { //$NON-NLS-1$
						if (setValidation) {
							throw new IllegalArgumentException("Duplicate validation flag"); //$NON-NLS-1$
						}
						setValidation = true;
						this.options.put(ENABLE_VALIDATION, "true"); //$NON-NLS-1$
						mode = DEFAULT_MODE;
						continue loop;
					}
					if (currentArg.equals("-o")) { //$NON-NLS-1$
						if (setOutputFile) {
							throw new IllegalArgumentException("Duplicate output file"); //$NON-NLS-1$
						}
						setOutputFile = true;
						mode = OUTPUT_FILE_MODE;
						continue loop;
					}
					if (currentArg.equals("-i")) { //$NON-NLS-1$
						if (setInputFile) {
							throw new IllegalArgumentException("Duplicate input file"); //$NON-NLS-1$
						}
						setInputFile = true;
						mode = INPUT_FILE_MODE;
						continue loop;
					}
					if (currentArg.equals("-r")) { //$NON-NLS-1$
						this.options.put(RECURSIVE, "true"); //$NON-NLS-1$
						mode = DEFAULT_MODE;
						continue loop;
					}
			}
		}
		final String input = (String) this.options.get(INPUT_SOURCE);
		if (input == null) {
			throw new IllegalArgumentException("An input file or directorty is required"); //$NON-NLS-1$
		}
		if (this.options.get(RECURSIVE) == null) {
			if (!input.toLowerCase().endsWith(".xml")) { //$NON-NLS-1$
				throw new IllegalArgumentException("Input file must be an xml file"); //$NON-NLS-1$
			}
		}
		this.options.put(CONVERTER_ID, String.valueOf(converterID));
	}

	public void parse2() throws ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		boolean validation = Boolean.valueOf((String) this.options.get(ENABLE_VALIDATION)).booleanValue();
		factory.setValidating(validation);
		factory.setIgnoringElementContentWhitespace(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		final String inputSourceOption = (String) this.options.get(INPUT_SOURCE);
		if (this.options.get(RECURSIVE) != null) {
			// collect all xml files and iterate over them
			File sourceDir = new File(inputSourceOption);
			if (!sourceDir.exists()) {
				throw new IllegalArgumentException("Directory " + inputSourceOption + " doesn't exist");//$NON-NLS-1$//$NON-NLS-2$
			}
			if (!sourceDir.isDirectory()) {
				throw new IllegalArgumentException(inputSourceOption + " must be a directory in recursive mode");//$NON-NLS-1$
			}
			File[] xmlFiles = getAllFiles(sourceDir, XML_FILTER);
			for (int i = 0, max = xmlFiles.length; i < max; i++) {
				final String inputFileName = xmlFiles[i].getAbsolutePath();
				InputSource inputSource = new InputSource(inputFileName);
				final String outputFileName = extractNameFrom(inputFileName);
				this.options.put(INPUT_SOURCE, inputFileName);
				this.options.put(OUTPUT_FILE_NAME, outputFileName);
				try {
					builder.setErrorHandler(new DefaultHandler() {
						public void error(SAXParseException e) throws SAXException {
							reportError(inputFileName, e);
							throw e;
						}
					});
					Document document = builder.parse(inputSource);
					LogDocumentNode documentNode = process(document);
					dump(documentNode);
				} catch (SAXException e) {
					System.out.println(e);
				} catch (IOException e) {
					// ignore
				}
			}
		} else {
			// Parse
			if (inputSourceOption != null) {
				InputSource inputSource = new InputSource(inputSourceOption);
				if (this.options.get(OUTPUT_FILE_NAME) == null) {
					this.options.put(OUTPUT_FILE_NAME, extractNameFrom(inputSourceOption));
				}
				try {
					builder.setErrorHandler(new DefaultHandler() {
						public void error(SAXParseException e) throws SAXException {
							reportError(inputSourceOption, e);
							throw e;
						}
					});
					Document document = builder.parse(inputSource);
					LogDocumentNode documentNode = process(document);
					dump(documentNode);
				} catch (SAXException e) {
					// ignore
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
	private void dump(LogDocumentNode documentNode) {
		IDOMConverter converter = ConverterFactory.createDOMConverter(Integer.parseInt((String) this.options.get(Converter.CONVERTER_ID)));
		converter.dump(CURRENT_FORMAT_VERSION, this.options, documentNode);
	}

	private LogDocumentNode process(Document document) {
		LogDocumentNode documentNode = new LogDocumentNode();
		NodeList nodeList=document.getElementsByTagName("problem_summary"); //$NON-NLS-1$
		if (nodeList.getLength() == 1) {
			Node problemSummaryNode = nodeList.item(0);
			NamedNodeMap problemSummaryMap = problemSummaryNode.getAttributes();
			ProblemSummaryNode summaryNode = new ProblemSummaryNode();
			documentNode.setProblemSummary(summaryNode);
			summaryNode.numberOfProblems = Integer.parseInt(problemSummaryMap.getNamedItem("problems").getNodeValue()); //$NON-NLS-1$
			summaryNode.numberOfErrors = Integer.parseInt(problemSummaryMap.getNamedItem("errors").getNodeValue()); //$NON-NLS-1$
			summaryNode.numberOfWarnings = Integer.parseInt(problemSummaryMap.getNamedItem("warnings").getNodeValue()); //$NON-NLS-1$
		}

		nodeList=document.getElementsByTagName("problems"); //$NON-NLS-1$
		if (nodeList == null) return null;

		final int length = nodeList.getLength();
		int globalErrorNumber = 1;
		for (int i = 0; i < length; i++) {
			Node problemsNode = nodeList.item(i);
			ProblemsNode node = new ProblemsNode();
			documentNode.addProblemsNode(node);
			Node sourceNode = problemsNode.getParentNode();
			NamedNodeMap sourceNodeMap = sourceNode.getAttributes();
			final String sourceFileName = sourceNodeMap.getNamedItem("path").getNodeValue();//$NON-NLS-1$
			node.sourceFileName = sourceFileName;
			NamedNodeMap problemsNodeMap = problemsNode.getAttributes();
			node.numberOfErrors = Integer.parseInt(problemsNodeMap.getNamedItem("errors").getNodeValue());//$NON-NLS-1$
			node.numberOfWarnings = Integer.parseInt(problemsNodeMap.getNamedItem("warnings").getNodeValue());//$NON-NLS-1$
			node.numberOfProblems = Integer.parseInt(problemsNodeMap.getNamedItem("problems").getNodeValue());//$NON-NLS-1$

			NodeList children = problemsNode.getChildNodes();
			int childrenLength = children.getLength();
			for (int j = 0; j < childrenLength; j++) {
				Node problemNode = children.item(j);
				NamedNodeMap problemNodeMap = problemNode.getAttributes();
				String severity = problemNodeMap.getNamedItem("severity").getNodeValue();//$NON-NLS-1$
				ProblemNode problem = new ProblemNode();
				final boolean isError = "ERROR".equals(severity);//$NON-NLS-1$
				problem.isError = isError;
				problem.id = problemNodeMap.getNamedItem("id").getNodeValue();//$NON-NLS-1$
				if (isError) {
					node.addError(problem);
				} else if (AbstractDOMConverter.FILTERED_WARNINGS_IDS.contains(problem.id)) {
					if (AbstractDOMConverter.FORBIDDEN_REFERENCE.equals(problem.id)) {
						node.addForbiddenWarning(problem);
					} else {
						node.addDiscouragedWarning(problem);
					}
				} else {
					node.addOtherWarning(problem);
				}
				problem.charStart = Integer.parseInt(problemNodeMap.getNamedItem("charStart").getNodeValue());//$NON-NLS-1$
				problem.charEnd = Integer.parseInt(problemNodeMap.getNamedItem("charEnd").getNodeValue());//$NON-NLS-1$
				problem.line = Integer.parseInt(problemNodeMap.getNamedItem("line").getNodeValue());//$NON-NLS-1$
				problem.globalProblemNumber = globalErrorNumber;
				problem.problemNumber = j;
				problem.sourceFileName = sourceFileName;
				globalErrorNumber++;
				NodeList problemChildren = problemNode.getChildNodes();
				int problemChildrenLength = problemChildren.getLength();
				for (int n = 0; n < problemChildrenLength; n++) {
					Node child = problemChildren.item(n);
					final String nodeName = child.getNodeName();
					if ("message".equals(nodeName)) {//$NON-NLS-1$
						NamedNodeMap childNodeMap = child.getAttributes();
						problem.message = childNodeMap.getNamedItem("value").getNodeValue();//$NON-NLS-1$
					} else if ("source_context".equals(nodeName)) {//$NON-NLS-1$
						NamedNodeMap childNodeMap = child.getAttributes();
						problem.sourceStart = Integer.parseInt(childNodeMap.getNamedItem("sourceStart").getNodeValue());//$NON-NLS-1$
						problem.sourceEnd = Integer.parseInt(childNodeMap.getNamedItem("sourceEnd").getNodeValue());//$NON-NLS-1$
						problem.contextValue = childNodeMap.getNamedItem("value").getNodeValue();//$NON-NLS-1$
					}
				}
			}
		}
		return documentNode;
	}

	private String extractNameFrom(String inputFileName) {
		int index = inputFileName.lastIndexOf('.');
		switch (Integer.parseInt((String) this.options.get(Converter.CONVERTER_ID))) {
			case ConverterFactory.TXT:
				return inputFileName.substring(0, index) + TXT_EXTENSION;
			case ConverterFactory.HTML:
				return inputFileName.substring(0, index) + HTML_EXTENSION;
			default:
				return inputFileName.substring(0, index) + HTML_EXTENSION;
		}
	}

	void reportError(final String inputFileName, SAXParseException e) {
		System.err.println("Error in " + inputFileName + " at line " + e.getLineNumber() + " and column " + e.getColumnNumber()); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
		System.err.println(e.getMessage());
	}

	public static void run(String[] args) throws ParserConfigurationException {
		Converter converter = new Converter();
		converter.configure(args);
		converter.parse2();
	}

	public static void main(String[] args) {
		try {
			run(args);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
}