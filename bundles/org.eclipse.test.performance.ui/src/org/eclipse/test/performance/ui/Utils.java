package org.eclipse.test.performance.ui;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.Variations;

/**
 * a utility class which provides html templates and other constant values.
 */
public class Utils {
	
	/**
	 *  HTML source used at beginning of html document.
	 */
	public static String HTML_OPEN="<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">";
	
	/**
	 *  Closing HTML tag </html>.
	 */
	public static String HTML_CLOSE="</html>";

	/**
	 *  Default style-sheet used on eclipse.org
	 */
	public static String HTML_DEFAULT_CSS="<style type=\"text/css\">"+
	"p, table, td, th {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"+
	"pre {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n"+
	"h2 { font-family: arial, helvetica, geneva; font-size: 18pt; font-weight: bold ; line-height: 14px}\n"+
	"code {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n" +
	"sup {  font-family: arial,helvetica,geneva; font-size: 10px}\n"+
	"h3 {  font-family: arial, helvetica, geneva; font-size: 14pt; font-weight: bold}\n"+
	"li {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"+
	"h1 {  font-family: arial, helvetica, geneva; font-size: 28px; font-weight: bold}\n"+
	"body {  font-family: arial, helvetica, geneva; font-size: 10pt; clip:   rect(   ); margin-top: 5mm; margin-left: 3mm}\n"+
	".indextop { font-size: x-large;; font-family: Verdana, Arial, Helvetica, sans-serif; font-weight: bold}\n"+
	".indexsub { font-size: xx-small;; font-family: Arial, Helvetica, sans-serif; color: #8080FF}\n"+
	"</style>";
	
	/**
	 * An utility object which stores a name, description and url associated for a performance configuration.
	 */
	public static class ConfigDescriptor{
		String name;
		String description;
		String url;
		String outputDir;
		
		/**
		 * 
		 * @param name the value specifed for the key config in the eclipse.perf.config system.property key value listings.  ie.  relengbuildwin2 (machine name)
		 * @param description a meaningful description to further describe the config.  ie.  win32-win32-x86 Sun 1.4.2_06
		 * @param url a url to results for this config.  Used in hyperlinks from graphs or tables.
		 */
		public ConfigDescriptor(String name, String description, String url,String outputDir){
			this.name=name;
			this.description=description;
			this.url=url;
			this.outputDir=outputDir;
		}
		
	}
	
	/**
	 * 
	 * @param configDescriptors a semi-colon separated listing of config descriptions.<br>
	 * Uses the following format:  name,description, url;name2, description2,url2;etc..
	 * @return a mapping of config names to their ConfigDescriptors.
	 */
	public static Hashtable getConfigDescriptors(String configDescriptors){
		//labelMappings in pairs separated by semi-colon ie. relengbuildwin2,win32-win32-x86;trelenggtk,linux-gtk-x86
		StringTokenizer tokenizer=new StringTokenizer(configDescriptors,";");
		Hashtable configMap = new Hashtable();
		
		while (tokenizer.hasMoreTokens()){
			String labelDescriptor=tokenizer.nextToken();
			int commaIndex=labelDescriptor.indexOf(",");
			String [] elements=labelDescriptor.split(",");
			String output=null;
			if (elements.length==4)
				output=elements[3];
			ConfigDescriptor descriptor=new ConfigDescriptor(elements[0],elements[1],elements[2],output);
			configMap.put(elements[0],descriptor);
		}
		return configMap;
	}
	
	/**
	 * Get all the scenarios for specified variations
	 * @return
	 */
	public static Scenario[] getScenarios(String buildIdPattern, String scenarioPattern,String config, String jvm){
	   	Dim[] qd = null;
	   	if (scenarioPattern==null)
	   		scenarioPattern="";
	   	
        Variations variations = getVariations(buildIdPattern,config,jvm);
        return DB.queryScenarios(variations, scenarioPattern+"%", PerformanceTestPlugin.BUILD, qd);
	}
	
	/**
	 * Creates a Variations object from 
	 * @param buildIdPattern
	 * @param config
	 * @param jvm
	 */
	public static Variations getVariations(String buildIdPattern, String config, String jvm){
        Variations variations = new Variations();
        variations.put(PerformanceTestPlugin.CONFIG, config);
        variations.put(PerformanceTestPlugin.BUILD, buildIdPattern);
        variations.put("jvm",jvm);
        return variations;
	}
	
	/**
	 * returns a list of components as taken from common prefixes to scenario names
	 * @param scenarios
	 * @param summaryEntries
	 * @param componentMapping
	 * @return
	 */
    public static ArrayList getComponentNames(Scenario[] scenarios){
    	ArrayList componentNames=new ArrayList();
    	
    	for (int i=0;i<scenarios.length;i++){
    		String prefix=null;
    		Scenario scenario=scenarios[i];
    		String scenarioName=scenario.getScenarioName();
    		
    		//use part of scenario name prior to .test to identify component
    		if (scenarioName.indexOf(".test")!=-1){
    			prefix=scenarioName.substring(0,scenarioName.indexOf(".test"));
    			if (!componentNames.contains(prefix))
    				componentNames.add(prefix);
    		}
    	}
    	return componentNames;
    }
    
    public static String printHtmlFingerPrint(FingerPrint fp){
    	String componentDescription=((Utils.ConfigDescriptor)fp.configDescriptors.get(fp.config)).description;
        String areas= fp.bar.getAreas();
        String output="";
            if (areas != null) {
                output="<h3>"+componentDescription+"</h3>";
            	output=output.concat("<img src=\"" + fp.getOutName() + ".jpeg\" usemap=\"#" + fp.outName + "\">"+
            	"<map name=\"" + fp.getOutName() + "\">"+areas+"</map>\n");
            } else {
            	output=output.concat("<br>There is no fingerprint for "+componentDescription+"<br><br>\n");
            }
            return output;
    }
    
    public static String printScenarioStatusTable(FingerPrint fp){       
            //print the component scenario table beneath the fingerprint
            fp.variations.put("config","%");
            ScenarioStatusTable sst=new ScenarioStatusTable(fp.variations,fp.component+"%",fp.configDescriptors);
            return sst.toString();
    }
}
