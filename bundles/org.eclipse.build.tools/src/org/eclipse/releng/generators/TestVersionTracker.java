package org.eclipse.releng.generators;

/**
 * This class finds the version of a feature, plugin, or fragment in a given
 * build source tree.
 */

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.SAXException;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;

public class TestVersionTracker extends DefaultHandler {

	private String installDirectory;
	private Hashtable elements;
	private SAXParser parser;
	
	//test
	public static void main(String[] args) {
		TestVersionTracker Tracker =
			new TestVersionTracker(args[0]);
		Tracker.parse(
			Tracker.installDirectory
				+ "/features/org.eclipse.sdk.tests-feature/feature.xml");
			Tracker.writeProperties(args[1], true);
	}

	public TestVersionTracker(String install) {
		//  Create a Xerces SAX Parser
        parser = new SAXParser();
        
        //  Set Content Handler
        parser.setContentHandler (this);
		
		// directory containing the source for a given build
		installDirectory = install;

		//  instantiate hashtable that will hold directory names with versions for elements
		elements = new Hashtable();
	}

    public void parse(String xmlFile){
			
	    //  Parse the Document      
        try {
            parser.parse(xmlFile);
        } catch (SAXException e) {
            System.err.println (e);
        } catch (IOException e) {
            System.err.println (e);
          
        }
    }

	//  Start Element Event Handler
	public void startElement(
		String uri,
		String local,
		String qName,
		Attributes atts) {

		String element = atts.getValue("id");
		String version = atts.getValue("version");

		if ((local.equals("plugin") || local.equals("fragment"))
			&& (new File(installDirectory + "/plugins/" + element + "/test.xml").exists())) {
				elements.put(element,element+"_"+version);
		} else if (local.equals("feature"))
				elements.put(element+"-feature",element+"_"+version);
	}

	public void writeProperties(String propertiesFile,boolean append){
		try{
			
		PrintWriter writer = new PrintWriter(new FileWriter(propertiesFile,append));
				
			Enumeration keys = elements.keys();

			while (keys.hasMoreElements()){
				Object key = keys.nextElement();
				writer.println(key.toString()+"="+elements.get(key).toString());
				writer.flush();
			}
		
		} catch (IOException e){
			System.out.println("Unable to write to file "+propertiesFile);
		}
		
		
	}

}
