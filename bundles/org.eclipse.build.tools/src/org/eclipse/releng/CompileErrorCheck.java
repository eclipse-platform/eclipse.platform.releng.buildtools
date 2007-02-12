/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng;

/**
 * A custom Ant task that finds compile logs containing compile
 * errors.  The compile logs with errors are sent as email attachments using
 * information in monitor.properties.
 */

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.Vector;
import java.util.Enumeration;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class CompileErrorCheck extends Task {

	//directory containing of build source, parent of features and plugins
	private String install = "";

	//keep track of compile logs containing errors
	private Vector logsWithErrors;

	public CompileErrorCheck() {
		logsWithErrors = new Vector();
	}

	public void execute() throws BuildException {
		findLogs(install);
		sendNotice();
	}

	// test
	public static void main(String[] args) {
		CompileErrorCheck checker = new CompileErrorCheck();
		checker.install="d:/compilelogs";
		checker.execute();
	}

	private void findLogs(String file) {
		File aFile = new File(file);

		// basis case
		if (aFile.isFile()) {
			if (aFile.getAbsolutePath().endsWith(".jar.bin.log")||aFile.getAbsolutePath().endsWith("dot.bin.log")){
				read(aFile);
			}else if(aFile.getAbsolutePath().endsWith(".xml")){
				parse(aFile);
			}
		} else {
			//recurse into directories looking for and reading compile logs
			File files[] = aFile.listFiles();

			for (int i = 0; i < files.length; i++) {
				findLogs(files[i].getAbsolutePath());
			}
		}
	}

	private void read(File file) {
		//read the contents of the log file, and return contents as a String
		if (file.length()==0)
			return;
		
		BufferedReader in = null;
		String aLine;

		try {
			in = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			while ((aLine = in.readLine()) != null) {
				int statusSummaryIndex=aLine.indexOf("problem (");
				if (statusSummaryIndex==-1)
					statusSummaryIndex=aLine.indexOf("problems (");
				
				if (statusSummaryIndex!=-1&&(aLine.indexOf("error", statusSummaryIndex) != -1)){
					logsWithErrors.add(file);
					System.out.println(file.getName()+" has compile errors.");
					return;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parse(File file) {
		Document aDocument=null;
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			InputSource inputSource = new InputSource(reader);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = null;

			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			try {
				aDocument = builder.parse(inputSource);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// Get summary of problems
			NodeList nodeList=aDocument.getElementsByTagName("problem_summary");
			if (nodeList==null)
				return;
			
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node problemSummaryNode = nodeList.item(i);
				NamedNodeMap aNamedNodeMap = problemSummaryNode.getAttributes();
				Node errorNode = aNamedNodeMap.getNamedItem("errors");
				if (errorNode!= null&&!errorNode.getNodeValue().equals("0")) {
					logsWithErrors.add(new File(file.getParentFile(),file.getName().replaceAll(".xml", ".html")));
					System.out.println(file.getName()+" has compile errors.");
					return;
				}
			}
	}
	
	private void sendNotice() {
		//send email notification that there are compile errors in the build
		//send the logs as attachments
		Enumeration enumeration = logsWithErrors.elements();

		if (logsWithErrors.size() > 0) {
			try{
			
			Mailer mailer = new Mailer();
			String [] logFiles = new String [logsWithErrors.size()];

			int i=0;
				
			while (enumeration.hasMoreElements()) {
				logFiles[i++]=((File) enumeration.nextElement()).getAbsolutePath();
			}

			mailer.sendMultiPartMessage("Compile errors in build", "Compile errors in build.  See attached compile logs.", logFiles);
			} catch (NoClassDefFoundError e){
				while (enumeration.hasMoreElements()) {
					String path=((File) enumeration.nextElement()).getAbsolutePath();
					String nameWithPlugin=path.substring(path.indexOf("plugins"),path.length());
					System.out.println("Compile errors detected in "+nameWithPlugin);
				}

				System.out.println("Unable to send email notice of compile errors.");
				System.out.println("The j2ee.jar may not be on the Ant classpath.");
				
			}

		}

	}

	/**
	 * Gets the install.
	 * @return Returns a String
	 */
	public String getInstall() {
		return install;
	}

	/**
	 * Sets the install.
	 * @param install The install to set
	 */
	public void setInstall(String install) {
		this.install = install;
	}

}
