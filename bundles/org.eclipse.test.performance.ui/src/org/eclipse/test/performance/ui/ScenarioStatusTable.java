/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.performance.ui;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.Variations;

public class ScenarioStatusTable {
	
	private Hashtable configMaps=null;
	private Variations variations;
	private String scenarioPattern;
	private ArrayList configNames=new ArrayList();
	private Hashtable scenarioComments;
	private final String baseline;
	
	private class ScenarioStatus{
		Hashtable statusMap;
		String name;
		Hashtable configStatus;
		boolean hasSlowDownExplanation=false;
						
		public ScenarioStatus(String scenarioName){
			name=scenarioName;
			statusMap=new Hashtable();
			configStatus=new Hashtable();
		}

	}
	
	/**
	 * Creates an HTML table of red x/green check for a scenario for each
	 * configuration.
	 * 
	 * @param variations
	 * @param scenarioPattern
	 * @param configDescriptors
	 */
	public ScenarioStatusTable(Variations variations,String scenarioPattern,Hashtable configDescriptors,Hashtable scenarioComments, String baseline){
		configMaps=configDescriptors;
		this.variations=variations;
		this.scenarioPattern=scenarioPattern;
		this.scenarioComments=scenarioComments;
		this.baseline= baseline;
	}
	
	/**
	 * Returns HTML representation of scenario status table.
	 */
	public String toString() {
		String OS="config";
		String htmlTable="";
        Scenario[] scenarios= DB.queryScenarios(variations, scenarioPattern, OS, null);
	
		if (scenarios != null && scenarios.length > 0) {
			ArrayList scenarioStatusList=new ArrayList();

			for (int i= 0; i < scenarios.length; i++) {
				Scenario scenario= scenarios[i];
				String scenarioName=scenario.getScenarioName();
//				if (!Utils.matchPattern(scenarioName, scenarioPattern)) continue;
				// returns the config names. Making assumption that indices in
				// the configs array map to the indices of the failure messages.
				String[] configs=scenario.getTimeSeriesLabels();
				String[] failureMessages= scenario.getFailureMessages();
				ScenarioStatus scenarioStatus=new ScenarioStatus(scenarioName);
				scenarioStatusList.add(scenarioStatus);

				String scenarioComment= (String)scenarioComments.get(scenarioName);
				if (scenarioComment != null)
					scenarioStatus.hasSlowDownExplanation= true;
				
				for (int j=0;j<configs.length;j++){
					if (!configNames.contains(configs[j]))
						configNames.add(configs[j]);

					double[] resultStats = Utils.resultStats(variations, scenarioName, baseline, configs[j]);
					int confidenceLevel = Utils.confidenceLevel(resultStats);
					
					boolean hasScenarioFailure = failureMessages[j] != null && failureMessages[j].indexOf(configs[j]) != -1; // ensure correct failure message relates to config
					StringBuffer buffer = new StringBuffer();
					if (hasScenarioFailure) {
						buffer.append(failureMessages[j]);
						if (scenarioStatus.hasSlowDownExplanation) {
							buffer.append(" - Explanation comment: ");
							buffer.append(scenarioComment);
						}
						confidenceLevel |= Utils.DEV;
					}

					scenarioStatus.configStatus.put(configs[j], new Integer(confidenceLevel));
					scenarioStatus.statusMap.put(configs[j], new Object[] { buffer.toString(), resultStats });
				}
			}
			
			String label=null;
			htmlTable=htmlTable.concat("<br><h4>Scenario Status</h4>\n" +
				"The scenario status table shows all scenarios tests result for all performance test machines. It gives a complete but compact view of performance result for the component.<br>\n" +
				"For each test (ie. in each cell of this table), following information are displayed:\n" +
				"<ul>\n" +
				"<li>an icon showing whether the test fails or passes and whether it's reliable or not.<br>\n" +
				"The legend for this icon is:\n" +
				"<ul>\n" +
				"<li>Green (<img src=\""+Utils.OK_IMAGE+"\">): mark a <b>successful result</b>, which means this test has neither significant performance regression nor significant standard error</li>\n" +
				"<li>Red (<img src=\""+Utils.FAIL_IMAGE+"\">): mark a <b>failing result</b>, which means this test shows a significant performance regression (more than 10%)</li>\n" +
				"<li>Gray (<img src=\""+Utils.FAIL_IMAGE_EXPLAINED+"\">): mark a <b>failing result</b> (see above) with a comment explaining this degradation.</li>\n" +
				"<li>Yellow (<img src=\""+Utils.FAIL_IMAGE_WARN+"\"> or <img src=\""+Utils.OK_IMAGE_WARN+"\">): mark a <b>failing or successful result</b> with a significant standard error (more than "+Utils.STANDARD_ERROR_THRESHOLD_STRING+")</li>\n" +
				"<li>\"n/a\": mark a test for with <b>no</b> performance results</li>\n" +
				"</ul></li>\n" +
				"<li>the value of the deviation from the baseline as a percentage (ie. formula is: <code>(build_test_time - baseline_test_time) / baseline_test_time</code>)</li>\n" +
				"<li>the value of the standard error of this deviation as a percentage (ie. formula is: <code>sqrt(build_test_stddev^2 / N + baseline_test_stddev^2 / N) / baseline_test_time</code>)<br>\n" +
				"Note that errors equal to 0 are not shown (tests which have only one iteration).</li>\n" +
				"</ul>" +
				"For failing tests, value of deviation with its standard error is added at the beginning of the error message you can see flying over the corresponding image.<br>\n" +
				"Follow the link on test box corresponding image for detailed results.<br>" +
				"<br>\n");

			htmlTable=htmlTable.concat("<table border=\"1\"><tr><td><h4>All "+scenarios.length+" scenarios</h4></td>\n");
			for (int i= 0; i < configNames.size(); i++){
				label=configNames.get(i).toString();
				String columnTitle=label;
				if (configMaps!=null) {
					Utils.ConfigDescriptor configDescriptor= (Utils.ConfigDescriptor)configMaps.get(label);
					if (configDescriptor != null)
						columnTitle=configDescriptor.description;
				}
				htmlTable=htmlTable.concat("<td><h5>"+columnTitle +"</h5></td>");
			}
			 
			htmlTable=htmlTable.concat("</tr>\n");
			
			// counter for js class Id's
			int jsIdCount=0;
			for (int j= 0; j < scenarioStatusList.size(); j++) {
				
				ScenarioStatus status=(ScenarioStatus)scenarioStatusList.get(j);

				htmlTable=htmlTable.concat("<tr><td>"+status.name.substring(status.name.indexOf(".",status.name.indexOf(".test")+1)+1)+"</td>");
				for (int i=0;i<configNames.size();i++){
					String message=null;
					String configName=configNames.get(i).toString();
					String aUrl=configName;
					double[] resultStats = null;
					if(status.statusMap.get(configName)!=null){
						Object[] statusInfo = (Object[]) status.statusMap.get(configName);
						message = (String) statusInfo[0];
						resultStats = (double[]) statusInfo[1];
					}

					if (status.statusMap.containsKey(configName)){
						int confidence = ((Integer) status.configStatus.get(configName)).intValue();
						String image = Utils.getImage(confidence, resultStats, status.hasSlowDownExplanation);
						StringBuffer html = new StringBuffer("\n<td><a ");
						if ((confidence & Utils.DEV) == 0 || message.length() == 0){
							// write deviation with error in table when test pass
							html.append("href=\"");
							html.append(aUrl);
							html.append('/');
							html.append(status.name.replace('#', '.').replace(':', '_').replace('\\', '_'));
							html.append(".html\">\n<img hspace=\"10\" border=\"0\" src=\"");
							html.append(image);
							html.append("\"/></a>");
						} else {
							// create message with tooltip text including deviation with error plus failure message
							jsIdCount+=1;
							html.append("class=\"tooltipSource\" onMouseover=\"show_element('toolTip");
							html.append(jsIdCount);
							html.append("')\" onMouseout=\"hide_element('toolTip");
							html.append(jsIdCount);
							html.append("')\" \nhref=\"");
							html.append(aUrl);
							html.append('/');
							html.append(status.name.replace('#', '.').replace(':', '_').replace('\\', '_'));
							html.append(".html\">\n<img hspace=\"10\" border=\"0\" src=\"");
							html.append(image);
							html.append("\"/>\n<span class=\"hidden_tooltip\" id=\"toolTip");
							html.append(jsIdCount);
							html.append("\">");
							html.append(message);
							html.append("</span></a>");
						}
						html.append(Utils.failureMessage(resultStats, false));
						html.append("</td>");
						htmlTable=htmlTable.concat(html.toString());
					}else{
						htmlTable=htmlTable.concat("<td>n/a</td>");
					}
				}
			}
			
			htmlTable=htmlTable.concat("</tr>\n");		
		}
		return htmlTable;
	}
}
