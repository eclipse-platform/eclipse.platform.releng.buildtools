/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.performance.ui;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.Variations;
import org.eclipse.test.internal.performance.eval.StatisticsUtil;
import org.eclipse.test.internal.performance.eval.StatisticsUtil.Percentile;

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
		NumberFormat percentFormatter= NumberFormat.getPercentInstance();
	
		if (scenarios != null && scenarios.length > 0) {
			ArrayList scenarioStatusList=new ArrayList();

			Percentile percentile= StatisticsUtil.T90;
			for (int i= 0; i < scenarios.length; i++) {
				Scenario scenario= scenarios[i];
				String scenarioName=scenario.getScenarioName();
				// returns the config names. Making assumption that indices in
				// the configs array map to the indeces of the failure messages.
				String[] configs=scenario.getTimeSeriesLabels();
				String[] failureMessages= scenario.getFailureMessages();
				ScenarioStatus scenarioStatus=new ScenarioStatus(scenarioName);
				scenarioStatusList.add(scenarioStatus);
				
				for (int j=0;j<configs.length;j++){
					
					boolean rejectNullHypothesis= Utils.rejectNullHypothesis(variations, scenarioName, baseline, configs[j]);
					scenarioStatus.configStatus.put(configs[j],new Boolean(rejectNullHypothesis));
					String failureMessage=rejectNullHypothesis?"":Utils.TTEST_FAILURE_MESSAGE;
					if (!configNames.contains(configs[j]))
						configNames.add(configs[j]);
					if (failureMessages[j]!=null){
						// ensure correct failure message relates to config
						if (failureMessages[j].indexOf(configs[j])!=-1){
							failureMessage=failureMessage.concat(failureMessages[j]);
							if (scenarioComments.containsKey(scenarioName)){
								failureMessage=failureMessage.concat(";  "+scenarioComments.get(scenarioName));
								scenarioStatus.hasSlowDownExplanation=true;
							}
						}
					}
					scenarioStatus.statusMap.put(configs[j],failureMessage);
				}
			}
			
			String label=null;
			htmlTable=htmlTable.concat("<br><h4>Scenario Status</h4>\n" +
					"The green/red indication is based on the assert condition in the test.  "+
					"Hover over <img src=\"FAIL.gif\"> for error message.<br>\n" +
					"Click on <img src=\"FAIL.gif\"> or <img src=\"OK.gif\"> for detailed results. <br>\n " +
					"Grayed images mark explainable degradations. <br>\n "+
					"Yellow images mark results where there is not enough evidence to reject the null hypothesis at the " + percentFormatter.format(percentile.inside()) + "level (Student's t-test).<br>\n" +
					"\"n/a\" - results not available.<br><br>\n");
			
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
						//the scenario has failed if is has a message other than the t-test message
						boolean scenarioFailed=!message.equals("")&&!message.equals(Utils.TTEST_FAILURE_MESSAGE);
						boolean isSignificant=((Boolean)status.configStatus.get(configName)).booleanValue();
						String image=null;			
						String successImage=isSignificant? Utils.OK_IMAGE : Utils.OK_IMAGE_WARN;
						String failImage=scenarioFailed&&isSignificant?Utils.FAIL_IMAGE:Utils.FAIL_IMAGE_WARN;
						
						if(scenarioFailed)
							image=failImage;
						else if(status.hasSlowDownExplanation)
							image=Utils.FAIL_IMAGE_EXPLAINED;
						else 
							image=successImage;
						if (aUrl!=null){
							String html="\n<td><a href=\""+aUrl+"/"+status.name.replace('#', '.').replace(':', '_').replace('\\', '_') 
							+ ".html"+"\">\n<img border=\"0\" src=\"" + image + "\"/></a></td>";
							
							//create message with tooltip text if there is a corresponding message
							if (message!=""){
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
							htmlTable=htmlTable.concat(scenarioFailed ?"<td><img title=\""+message+"\" border=\"0\" src=\""+failImage+"\"/></td>" 
									:"<td><img border=\"0\" src=\""+successImage+"\"/></td>");
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
