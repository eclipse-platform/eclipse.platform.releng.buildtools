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
package org.eclipse.performance.graph;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import junit.framework.AssertionFailedError;

import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.SummaryEntry;
import org.eclipse.test.internal.performance.db.TimeSeries;
import org.eclipse.test.internal.performance.db.Variations;



public class HtmlView {

    public static  void main (String[] args) {
    	
    	String [] bgColors= {"#DDDDDD","#EEEEEE"};
    	String reference=args[0];
        String [] buildTypeFilter={reference,args[1]+"%"};
    	String resultsFolder=args[2];
		String outFile= null;
		PrintStream ps= null;
       // get all Scenarios 
        Dim[] qd= null; // new Dim[] { InternalDimensions.CPU_TIME };
        Variations variations= new Variations();
        variations.put(PerformanceTestPlugin.CONFIG, "relengbuildwin2");       
        variations.put(PerformanceTestPlugin.BUILD, "%");
        Scenario[] scenarios=DB.queryScenarios(variations, "org.eclipse%", PerformanceTestPlugin.BUILD, qd);
        
        
        Grapher grapher =new Grapher(scenarios,resultsFolder+"/graphs",reference);

        for (int s= 0; s < scenarios.length; s++) {
            Scenario t= scenarios[s];
            outFile=resultsFolder+"/"+t.getScenarioName().replace('#','.').replace(':','_').replace('\\','_')+".html";
    		if (outFile != null) {
    		    try {
                    ps= new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
                } catch (FileNotFoundException e) {
                    System.err.println("can't create output file"+outFile); //$NON-NLS-1$
                }
    		}
    		if (ps == null)
    		    ps= System.out;
   	        ps.println("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">");
   	        ps.println("<link rel=\"stylesheet\" href=\"../../default_style.css\" type=\"text/css\">");
   	        ps.println("<title>Eclipse Performance Data</title></head>"); //$NON-NLS-1$
            ps.println("<body>Scenario: " + t.getScenarioName()+"<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$

            ps.println("<font size=\"-6\"><table>"); //$NON-NLS-1$ //$NON-NLS-2$

            
            String[] timeSeriesLabels= t.getTimeSeriesLabels();
            ps.println("<tr><td>Builds:</td>"); //$NON-NLS-1$
            for (int j= 0; j < timeSeriesLabels.length; j++)
                ps.println("<td bgcolor="+bgColors[(j+3)%2] +">"+timeSeriesLabels[j]+"</td>");
            ps.println("</tr>");
                                    
            Dim[] dimensions= t.getDimensions();
            for (int i= 0; i < dimensions.length; i++) {
                Dim dim= dimensions[i];
                try{
                ps.println("<tr><td><a href=\"graphs/"+t.getScenarioName().replace('#','.').replace(':','_').replace('\\','_')+"_"+dim.getName()+".html\">"+dim.getName() + ':'+"</a></td>");
                TimeSeries ts= t.getTimeSeries(dim);
 
                int n= ts.getLength();
                for (int j= 0; j < n; j++) {
    	            String stddev= ""; //$NON-NLS-1$
                    double stddev2= ts.getStddev(j);
    	            if (stddev2 != 0.0)
    	                stddev= " [" + dim.getDisplayValue(ts.getStddev(j)) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
                    ps.println("<td bgcolor="+bgColors[(j+3)%2] +">"+dim.getDisplayValue(ts.getValue(j)) + stddev+"</td>");
                }
                } catch (AssertionFailedError e){
                	e.printStackTrace();
                	continue;
                }
                ps.println("</tr>"); //$NON-NLS-1$ //$NON-NLS-2$            
            }
            ps.println();      
            ps.println("</font></table><br><br>");
            ps.println("</body></html>"); //$NON-NLS-1$
            if (ps != System.out)
                ps.close();
        }   
    }

}
