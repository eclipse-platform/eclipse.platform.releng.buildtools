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

public class DimensionHistories {

    private static final int GRAPH_HEIGHT= 300;
    private static final int GRAPH_WIDTH= 800;

    String testResultDirectory;
    String outputDirectory;
    String referenceBuildId;
    String buildTypeFilter;
    Scenario [] scenarios;
    
     public DimensionHistories(Scenario [] scenarios,String output,String reference) {
    	this.scenarios=scenarios;
    	outputDirectory=output;
    	referenceBuildId=reference;
    	run();
    }

    public void run() {

        Display display= Display.getDefault();
        Color black= display.getSystemColor(SWT.COLOR_BLACK);
        Color green= display.getSystemColor(SWT.COLOR_DARK_GREEN);

        new File(outputDirectory).mkdirs();

         for (int s= 0; s < scenarios.length; s++) {
            Scenario t= scenarios[s];
            String scenarioName= t.getScenarioName();
            Dim[] dimensions= t.getDimensions();
            for (int i= 0; i < dimensions.length; i++) {
                Dim dim= dimensions[i];
                String dimensionName= dim.getName();
                LineGraph graph= new LineGraph(scenarioName + ": " + dimensionName, dim);
                TimeSeries ts=null;
                try{
                ts= t.getTimeSeries(dim);
                int n= ts.getLength();
                
                if (n > 0) {
	                for (int j= 0; j < n; j++) {
	                    String buildID= ts.getLabel(j);
	                    double value= ts.getValue(j);
	                    Color c= buildID.indexOf(referenceBuildId)  >= 0 ? green : black;
	                    int underscoreIndex=buildID.indexOf('_');
	                    buildID=(buildID.indexOf('_')==-1)?buildID:buildID.substring(0,underscoreIndex);
	                    if (c == green)
	                    	graph.addItem(buildID, dim.getDisplayValue(value), value, c, true);
	                    else
	                    	graph.addItem(buildID, dim.getDisplayValue(value), value, c, (n-2<j) );	
	                }
	                
	                drawGraph(graph, outputDirectory + "/" + scenarioName.replace('#', '.').replace(':','_').replace('\\','_') + "_" + dimensionName);
	                }
                } catch (AssertionFailedError e){
                	//System.err.println("Unable to get result for: "+t.getScenarioName()+" "+ts.toString());
                }
            }
        }
    }

    public void drawGraph(LineGraph p, String output) {

        Image image= new Image(Display.getDefault(), GRAPH_WIDTH, GRAPH_HEIGHT);

        p.paint(image);

        ImageLoader il= new ImageLoader();
        il.data= new ImageData[] { image.getImageData()};

        OutputStream out= null;
        try {
            out= new BufferedOutputStream(new FileOutputStream(output + ".jpeg"));
            //System.out.println("writing: " + output);
            il.save(out, SWT.IMAGE_JPEG);
            
            String areas= p.getAreas();
            String scenarioName=output.substring(output.lastIndexOf('/')+1);
            if (areas != null) {
    	        try {
    	            PrintStream os= new PrintStream(new FileOutputStream(output + ".html"));
     	            os.println(Utils.HTML_OPEN);//"<html><body>");
     	            os.println("<body>");
    	            os.println(Utils.HTML_MAP_MOUSE_OVER_JS);//"<script language=\"JavaScript\">");
    	            os.println("<h3>"+p.fTitle+"</h3>");
    	            os.println("<img src=\"" + scenarioName + ".jpeg\" usemap=\"#" + scenarioName + "\">");
    	            os.println("<map name=\"" + scenarioName + "\">");
    	            os.println(areas);
    	            os.println("</map></body>");
    	            os.println(Utils.HTML_CLOSE);
    	            os.close();
    	        } catch (FileNotFoundException e) {
    	            // TODO Auto-generated catch block
    	            e.printStackTrace();
    	        }
            }
            
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            image.dispose();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    // silently ignored
                }
            }
        }
    }
}