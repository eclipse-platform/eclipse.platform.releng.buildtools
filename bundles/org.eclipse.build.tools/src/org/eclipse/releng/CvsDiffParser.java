/*
 * Created on Dec 9, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.releng;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.Enumeration;

/**
 * @author kmoir
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CvsDiffParser extends Task {
	
	private String mapDiffFile;
	private String mapOwnerProperties;

	/**
	 * 
	 */
	public CvsDiffParser() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		
		CvsDiffParser Parser = new CvsDiffParser();
		Parser.mapDiffFile = "c:\\temp\\Mapfiles\\compareMaps.txt";
		System.out.println(Parser.parseMapDiffFile().toString());
	}
	
	public void execute() throws BuildException {
	}
	

	/**
	 * @return Returns the mapDiffFile.
	 */
	public String getMapDiffFile() {
		return mapDiffFile;
	}

	/**
	 * @param mapDiffFile The mapDiffFile to set.
	 */
	public void setMapDiffFile(String mapDiffFile) {
		this.mapDiffFile = mapDiffFile;
	}

	/**
	 * @return Returns the mapOwnerProperties.
	 */
	public String getMapOwnerProperties() {
		return mapOwnerProperties;
	}

	/**
	 * @param mapOwnerProperties The mapOwnerProperties to set.
	 */
	public void setMapOwnerProperties(String mapOwnerProperties) {
		this.mapOwnerProperties = mapOwnerProperties;
	}

	private Vector parseMapDiffFile () {
		Vector updateList = new Vector();
		
//read the contents of the Diff file, and return contents as a String
		if (mapDiffFile.length()==0)
			return null;
		
		BufferedReader in = null;
		String aLine;
		String contents = "";

		try {
			in = new BufferedReader(new FileReader(mapDiffFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			while ((aLine = in.readLine()) != null) {
				if (aLine.startsWith("RCS file")){
                    String mapPath = (aLine.substring(aLine.indexOf(":"),aLine.indexOf(","))).trim();
                    updateList.add(new File(mapPath).getName());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return updateList;
	}
	
	private Vector parseComponentsProperties(){
		Vector componentList = new Vector();
		
		
// the Object that holds the key value pairs in monitor.properties
	}
	

		public Component[] getComponents() {
			
			Properties componentProperties;
			
			componentProperties = new Properties();
			// retrieve information from monitor.properties file.
			//  This file should reside in the same directory as the startup.jar at build time.
			try {
				componentProperties.load(
						new FileInputStream(new File(mapOwnerProperties)));
			}
			catch (IOException e) {}
								
			Component[] components = new Component[componentProperties.size()];
			
			Enumeration propKeys = componentProperties.keys();
			int i = 1; 
			
			while (propKeys.hasMoreElements()) {
				components[i++] = getComponent(propKeys.nextElement().toString(), componentProperties.getProperty(propKeys.nextElement().toString()));
			}
	
							
   return components;		
						
		}
		
		private Component getComponent(String componentName, String mapList) {
			
			Component component = new Component();
			component.setComponentName(componentName);
		
			// Create a vector of map names from the map list //
			StringTokenizer str = new StringTokenizer(mapList,",");
			while (str.hasMoreTokens()) {
				component.getMaps().add(str.nextToken());
				
							}
		
			return component;
		}
		
		
	}
		


	
	

