/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.performance.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import junit.framework.AssertionFailedError;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.TimeSeries;

public class ScenarioResults {
	private Scenario[] scenarios;

	private String reference;

	private String resultsFolder;

	private String configName;

	private String current;

	/**
	 * An html table representation for a scenario of all dimensions stored in a
	 * performance database for all builds.
	 * 
	 * @param scenarios
	 *            The list of scenarios for which to create the tables.
	 * @param reference
	 *            The reference build to highlight in the table.
	 * @param resultsFolder
	 *            The output folder for the html files generated. The results
	 *            folder will contain a graphs subdirectory which will contain
	 *            line graphs of the a specified dimension measurement over time
	 *            for a specific scenario.
	 * @param object
	 */
	public ScenarioResults(Scenario[] scenarios, String reference,
			String resultsFolder, String configName, String current, Utils.ConfigDescriptor configDescriptor) {
		this.scenarios = scenarios;
		this.reference = reference;
		this.resultsFolder = resultsFolder;
		this.configName = configName;
		if (configDescriptor!=null){
			this.configName=configDescriptor.description;
			this.resultsFolder=configDescriptor.outputDir;
		}
		this.current = current;
		run();
	}

	public void run() {

		String[] bgColors = { "#DDDDDD", "#EEEEEE" };
		String outFile = null;
		PrintStream ps = null;

		for (int s = 0; s < scenarios.length; s++) {
			Scenario t = scenarios[s];
			int []buildNameIndeces={-1,-1};
			buildNameIndeces[0]=Utils.getBuildNameIndex(t.getTimeSeriesLabels(),current);
			buildNameIndeces[1]=Utils.getBuildNameIndex(t.getTimeSeriesLabels(),reference);
			//don't produce result if none exists for current build
			if (Utils.getBuildNameIndex(t.getTimeSeriesLabels(),current)==-1) {
				continue;
			} 
			

			String scenarioFileName=t.getScenarioName().replace('#', '.').replace(':', '_')
			.replace('\\', '_');
			outFile = resultsFolder
					+ "/"
					+ scenarioFileName + ".html";
			if (outFile != null) {
				try {
					new File(outFile).getParentFile().mkdirs();
					ps = new PrintStream(new BufferedOutputStream(
							new FileOutputStream(outFile)));
				} catch (FileNotFoundException e) {
					System.err.println("can't create output file" + outFile); //$NON-NLS-1$
				}
			}
			if (ps == null)
				ps = System.out;
			ps.println(Utils.HTML_OPEN);// <html><head><meta
			// http-equiv=\"Content-Type\"
			// content=\"text/html;
			// charset=iso-8859-1\">");
			ps.println(Utils.HTML_DEFAULT_CSS);
			ps.println("<title>"+t.getScenarioName() + "("+configName+")"+"</title></head>"); //$NON-NLS-1$
			ps.println("<h4>Scenario: " + t.getScenarioName() + "("+configName+")</h4><br>"); //$NON-NLS-1$ //$NON-NLS-2$
			ps.println("Click measurement name to view line graph of measured values over builds.  Click <a href=\"http://download.eclipse.org/downloads/performance/dimensions.html\">here</a> for measurement descriptions.<br>");
			ps.println("Values in red and green indicate degradation > 10% and improvement > 10%,respectively.<br>");
			ps.println("<table border=\"1\">"); //$NON-NLS-1$ //$NON-NLS-2$

			Dim[] dimensions = t.getDimensions();
			try {
				ps.println("<tr><td>Build Id</td>"); //$NON-NLS-1$
				for (int i = 0; i < dimensions.length; i++) {
					Dim dim = dimensions[i];
					ps.println("<td><a href=\"#"+ configName+"_"+scenarioFileName+"_"+ dim.getName() +"\">" + dim.getName()
							+ "</a></td>");
				}
				ps.print("</tr>\n");

				//store current and reference values for diff later
				double [][] diffValues=new double [2][dimensions.length];
				//to determine if diff is possible
				boolean [] refValueExistance=new boolean[dimensions.length];
				
				for (int j = 0; j < 2; j++) {
					String referenceIndicator = (j == 1) ? "(reference)" : "";

					String buildName = ((buildNameIndeces[j] == -1) ? "n/a" : t
							.getTimeSeriesLabels()[buildNameIndeces[j]])
							+ referenceIndicator;
					ps.print("<tr><td>" + buildName + "</td>");
										
					for (int i = 0; i < dimensions.length; i++) {
						Dim dim = dimensions[i];
						TimeSeries ts = t.getTimeSeries(dim);
						if (j==1&&buildNameIndeces[j]!=-1)
							refValueExistance[i]=true;

						if (buildNameIndeces[j] != -1)
							diffValues[j][i]=ts.getValue(buildNameIndeces[j]);
						
						String displayValue = (buildNameIndeces[j] == -1) ? "n/a"
								: dim.getDisplayValue(ts
										.getValue(buildNameIndeces[j]));
						String stddev = (buildNameIndeces[j] == -1) ? "0" : dim
								.getDisplayValue(ts
										.getStddev(buildNameIndeces[j]));

						if (stddev.startsWith("0 ") || stddev.equals("0"))
							ps.println("<td>" + displayValue + "</td>");
						else
							ps.println("<td>" + displayValue + " [" + stddev
									+ "]" + "</td>");
					}
					ps.print("</tr>");
				}
				//get diffs and print results
				ps.println(getDiffs(diffValues,dimensions,refValueExistance));
				ps.println();
				ps.println("</font></table>");
			
				// print image maps of historical
				for (int i = 0; i < dimensions.length; i++) {
					Dim dim = dimensions[i];
					String dimName=dim.getName();
					LineGraph lg=Utils.getLineGraph(t,dim.getName(),reference,current);
					String lgImg=resultsFolder+"/graphs/"+scenarioFileName+"_"+dimName+".gif";
					Utils.printLineGraphGif(lg,lgImg);
					ps.println("<a name=\""+configName+"_"+scenarioFileName+"_"+ dimName+"\"></a>");
					ps.println("<h4>"+dimName+"</h4>");
					ps.println(Utils.getImageMap(lg,"graphs/"+scenarioFileName+"_"+dimName+".gif"));
					// ps.println(new
					// DimensionHistories(t,resultsFolder+"/graphs",reference,configName).getImageMap(dim));
				}
				ps.println("<br><br></body>");
				ps.println(Utils.HTML_CLOSE); //$NON-NLS-1$
				if (ps != System.out)
					ps.close();

			} catch (AssertionFailedError e) {
				e.printStackTrace();
				continue;
			}
		}
	}

	private int[] getBuildNameIndeces(String[] timeSeriesLabels,
			String current, String baseline) {
		int[] indeces = { -1, -1 };
		for (int i = 0; i < timeSeriesLabels.length; i++) {
			String timeSeriesLabel = timeSeriesLabels[i];
			if (timeSeriesLabel.equals(current))
				indeces[0] = i;
			if (timeSeriesLabel.equals(baseline))
				indeces[1] = i;
		}
		return indeces;
	}
	
	private String getDiffs(double [][]values,Dim[] dimensions, boolean []refValueExistance){
		String diffRow="<tr><td>Delta</td>";
		for (int j=0;j<dimensions.length;j++){
			Dim dim = dimensions[j];
			double diffValue=values[0][j]-values[1][j];
			int diffPercentage=0;
			if (values[1][j]!=0)
				diffPercentage=(int)(((diffValue/values[1][j])*100)*100.0)/100;
			String diffDisplayValue=dim.getDisplayValue(diffValue);
			//green
			String fontColor="";
			if ((diffPercentage<-10&&!dim.largerIsBetter())||(diffPercentage>10&&dim.largerIsBetter()))
				fontColor="#00FF00";
			if ((diffPercentage<-10&&dim.largerIsBetter())||(diffPercentage>10&&!dim.largerIsBetter()))
				fontColor="#FF0000";

			diffPercentage=Math.abs(diffPercentage);
			String percentage=(diffPercentage==0)?"":"<br>"+diffPercentage+" %";

			if (diffPercentage>10 || diffPercentage<-10){
				diffRow=diffRow.concat("<td><FONT COLOR=\""+fontColor+"\">"+diffDisplayValue+percentage+"</FONT></td>");
			} else if(refValueExistance[j]){
				diffRow=diffRow.concat("<td>"+diffDisplayValue+percentage+"</td>");
			}else{
				diffRow=diffRow.concat("<td>n/a</td>");
			}
		}
		diffRow=diffRow.concat("</tr>");
		return diffRow;
	}
	
//	private Dim[] filteredDimensions(ArrayList dimensionNames,Dim[] dimensions){
//		ArrayList list = new ArrayList();
//		for (int i=0;i<dimensions.length;i++){
//			Dim dim=dimensions[i];
//			if (!dimensionNames.contains(dim.getName()))
//				list.add(dim);
//		}
//		return list.toArray();
//	}


}
