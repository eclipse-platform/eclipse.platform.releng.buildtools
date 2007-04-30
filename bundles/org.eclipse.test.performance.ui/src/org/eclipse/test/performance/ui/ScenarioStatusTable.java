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
					String failureMessage = Utils.failureMessage(resultStats, false);
					int confidenceLevel = Utils.confidenceLevel(resultStats);
					
					boolean hasScenarioFailure = failureMessages[j] != null && failureMessages[j].indexOf(configs[j]) != -1; // ensure correct failure message relates to config
					if (hasScenarioFailure || failureMessage != null){
						StringBuffer buf= new StringBuffer();
						if (hasScenarioFailure) {
							buf.append(failureMessages[j]);
							if (scenarioStatus.hasSlowDownExplanation) {
								buf.append("\n - Explanation comment: ");
								buf.append(scenarioComment);
							}
							confidenceLevel |= Utils.DEV;
						}
						if (failureMessage != null) {
							if (buf.length() > 0) buf.append("\n - ");
							buf.append(failureMessage);
						}
						failureMessage = buf.toString();
					} else {
						failureMessage = "";
					}

					scenarioStatus.configStatus.put(configs[j], new Integer(confidenceLevel));
					scenarioStatus.statusMap.put(configs[j], failureMessage);
				}
			}
			
			String label=null;
			htmlTable=htmlTable.concat("<br><h4>Scenario Status</h4>\n" +
				"Click on test box corresponding image (<img src=\"FAIL.gif\"> or <img src=\"OK.gif\">) for detailed results.<br>\n" +
				"For all other images than  <img src=\"OK.gif\">, fly over it to get corresponding error/warning/info message.<br>\n" +
				"The images color legend is:\n" +
				"<ul>\n" +
				"<li>Red (<img src=\"FAIL.gif\">): indicates that the assert condition failed for the test</li>\n" +
				"<li>Grayed (<img src=\"FAIL_greyed.gif\">): mark explainable degradations.</li>\n" +
				"<li>Light (<img src=\"FAIL_err.gif\"> or <img src=\"OK_err.gif\">): mark results with standard error higher than "+Utils.STANDARD_ERROR_THRESHOLD_STRING+"</li>\n" +
				"<li>Blue (<img src=\"FAIL_ttest.gif\"> or <img src=\"OK_ttest.gif\">): mark results where Student's t-test failed (see below) but was moderated by the fact that the error is less than "+Utils.STANDARD_ERROR_THRESHOLD_STRING+"</li>\n" +
				"<li>Yellow (<img src=\"FAIL_caution.gif\"> or <img src=\"OK_caution.gif\">): mark results where Student's t-test failed which means that there is not enough evidence to reject the null hypothesis at the 90% level.<br>\n" +
				"<li>\"n/a\": mark not available results.</li>\n" +
				"</ul><br>\n");
			
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
					if(status.statusMap.get(configName)!=null){
						message=status.statusMap.get(configName).toString();
					}

					if (status.statusMap.containsKey(configName)){
						int confidence = ((Integer) status.configStatus.get(configName)).intValue();
						String image = Utils.getImage(confidence, status.hasSlowDownExplanation);
						if (aUrl!=null){
							String html="\n<td><a href=\""+aUrl+"/"+status.name.replace('#', '.').replace(':', '_').replace('\\', '_') 
							+ ".html"+"\">\n<img border=\"0\" src=\"" + image + "\"/></a></td>";
							
							//create message with tooltip text if there is a corresponding message
							if (message.length() > 0){
								jsIdCount+=1;
								html="<td><a " +
								"class=\"tooltipSource\" onMouseover=\"show_element('toolTip"+(jsIdCount)+"')\"" +
								" onMouseout=\"hide_element('toolTip"+(jsIdCount)+"')\" "+
								"\nhref=\""+aUrl+"/"+status.name.replace('#', '.').replace(':', '_').replace('\\', '_')+".html"+"\">" +
								"<img border=\"0\" src=\""+image+"\"/>" +
								"\n<span class=\"hidden_tooltip\" id=\"toolTip"+jsIdCount+"\">"+message+"</span></a></td>"+ 
								"";
							}
							htmlTable=htmlTable.concat(html);
						} else{
							htmlTable=htmlTable.concat("<td><img title=\""+message+"\" border=\"0\" src=\""+image+"\"/></td>");
						}	
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
