package org.eclipse.test.performance.ui;

import java.util.Hashtable;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.Variations;

public class ScenarioStatusTable {
	
	protected Hashtable configMaps=null;
	private Variations variations;
	private String scenarioPattern;

	public ScenarioStatusTable(Variations variations,String scenarioPattern,String configDescriptors){
		configMaps=Utils.getConfigDescriptors(configDescriptors);
		this.variations=variations;
		this.scenarioPattern=scenarioPattern;
		
	}
	
	
	public String toString() {
		String OS="config";
		String htmlTable="";
        Scenario[] scenarios= DB.queryScenarios(variations, scenarioPattern, OS, null);
	
		if (scenarios != null && scenarios.length > 0) {
			Scenario scenario= scenarios[0];

			String[] labels= scenario.getTimeSeriesLabels();
			htmlTable=htmlTable.concat("<table border=\"1\"><tr><td><h4>All "+scenarios.length+" scenarios</h4></td>\n");
			String label=null;
			for (int i= 0; i < labels.length; i++){
				label=labels[i];
				htmlTable=htmlTable.concat("<td><h5>"+((Utils.ConfigDescriptor)configMaps.get(label)).description +"</h5></td>");
			}
			htmlTable=htmlTable.concat("</tr>\n");
           
			for (int i= 0; i < scenarios.length; i++) {
				scenario= scenarios[i];
				String scenarioName=scenario.getScenarioName();
				htmlTable=htmlTable.concat("<tr><td>"+scenarioName.substring(scenarioName.indexOf(".",scenarioName.indexOf(".test")+1)+1)+"</td>");

				String[] failureMessages= scenario.getFailureMessages();
				
				for (int j= 0; j < failureMessages.length; j++) {
					String message= failureMessages[j];
					// show results as red/green with failure messages displayed in a tooltip with hyperlink to html table of all measurements
					String aUrl=((Utils.ConfigDescriptor)configMaps.get(label)).url;
					htmlTable=htmlTable.concat(message != null ? 
							"<td><a title=\""+message+"\" href=\""
							+aUrl+"/"+scenario.getScenarioName().replace('#', '.').replace(':', '_').replace('\\', '_') 
							+ ".html"+" \"><img border=\"0\" src=\"../../FAIL.gif\"></a></td>" 
							:"<td><a href=\""+aUrl+"/"+scenario.getScenarioName().replace('#', '.').replace(':', '_').replace('\\', '_') 
							+ ".html"+" \"><img border=\"0\" src=\"../../OK.gif\"></a></td>");
				}         
				htmlTable=htmlTable.concat("</tr>\n");
			}			
		}
		
		return htmlTable;
	}
}
