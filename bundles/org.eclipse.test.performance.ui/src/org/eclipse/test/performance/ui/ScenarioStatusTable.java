package org.eclipse.test.performance.ui;

import java.util.ArrayList;
import java.util.Hashtable;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.Variations;

public class ScenarioStatusTable {
	
	protected Hashtable configMaps=null;
	private Variations variations;
	private String scenarioPattern;
	private ArrayList configNames=new ArrayList();
	
	private class ScenarioStatus{
		Hashtable statusMap;
		String name;
		Object [] configs;
		
		public ScenarioStatus(String scenarioName){
			name=scenarioName;
			statusMap=new Hashtable();
		}
		private Object[] getConfigs(){
			return statusMap.keySet().toArray();
		}
	}
	
	public ScenarioStatusTable(Variations variations,String scenarioPattern,Hashtable configDescriptors){
		configMaps=configDescriptors;
		this.variations=variations;
		this.scenarioPattern=scenarioPattern;
	}
	
	
	public String toString() {
		String OS="config";
		String htmlTable="";
        Scenario[] scenarios= DB.queryScenarios(variations, scenarioPattern, OS, null);
	
		if (scenarios != null && scenarios.length > 0) {
			ArrayList scenarioStatusList=new ArrayList();

			for (int i= 0; i < scenarios.length; i++) {
				Scenario scenario= scenarios[i];
				String scenarioName=scenario.getScenarioName();
				//returns the config names.  Making assumption that indices  in the configs array map to the indeces of the failure messages.
				String[] configs=scenario.getTimeSeriesLabels();
				String[] failureMessages= scenario.getFailureMessages();
				ScenarioStatus scenarioStatus=new ScenarioStatus(scenarioName);
				scenarioStatusList.add(scenarioStatus);
			
				for (int j=0;j<configs.length;j++){
					String failureMessage="";
					if (!configNames.contains(configs[j]))
						configNames.add(configs[j]);
					if (failureMessages[j]!=null)
						failureMessage=failureMessages[j];
						scenarioStatus.statusMap.put(configs[j],failureMessage);		
				}
			}
			
			String label=null;
			htmlTable=htmlTable.concat("<table border=\"1\"><tr><td><h4>All "+scenarios.length+" scenarios</h4></td>\n");
			for (int i= 0; i < configNames.size(); i++){
				label=configNames.get(i).toString();
				htmlTable=htmlTable.concat("<td><h5>"+((Utils.ConfigDescriptor)configMaps.get(label)).description +"</h5></td>");
			}
			 
			htmlTable=htmlTable.concat("</tr>\n");
			for (int j= 0; j < scenarioStatusList.size(); j++) {
				
				ScenarioStatus status=(ScenarioStatus)scenarioStatusList.get(j);

				htmlTable=htmlTable.concat("<tr><td>"+status.name.substring(status.name.indexOf(".",status.name.indexOf(".test")+1)+1)+"</td>");

				for (int i=0;i<configNames.size();i++){
					String message=null;
					String aUrl=null;
					String configName=configNames.get(i).toString();
					if(status.statusMap.get(configName)!=null){
						message=status.statusMap.get(configName).toString();
					    aUrl=((Utils.ConfigDescriptor)configMaps.get(configName)).url;
					}

					if (status.statusMap.containsKey(configName)){
						htmlTable=htmlTable.concat(message != "" ?"<td><a title=\""+message+"\" href=\""
								+aUrl+"/"+status.name.replace('#', '.').replace(':', '_').replace('\\', '_') 
								+ ".html"+" \"><img border=\"0\" src=\"../FAIL.gif\"></a></td>" 
								:"<td><a href=\""+aUrl+"/"+status.name.replace('#', '.').replace(':', '_').replace('\\', '_') 
								+ ".html"+" \"><img border=\"0\" src=\"../OK.gif\"></a></td>");
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
