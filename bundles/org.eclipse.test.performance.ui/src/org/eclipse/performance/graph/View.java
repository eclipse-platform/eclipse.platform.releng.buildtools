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

import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.TimeSeries;



public class View {

    public static  void main (String[] args) {
		String buildTypeFilter=args[0];
    	String resultsFolder=args[1];
		String outFile= null;
		PrintStream ps= null;

        // get all Scenarios 
        Dim[] qd= null; // new Dim[] { InternalDimensions.CPU_TIME };
        
        Scenario[] scenarios= DB.queryScenarios("relengbuildwin2",buildTypeFilter+"%", "%", qd); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Grapher grapher =new Grapher(scenarios,resultsFolder);
 
 
        
        for (int s= 0; s < scenarios.length; s++) {
            Scenario t= scenarios[s];
            outFile=resultsFolder+"/"+t.getScenarioName().replace('#','.')+".html";
    		if (outFile != null) {
    		    try {
                    ps= new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
                } catch (FileNotFoundException e) {
                    System.err.println("can't create output file"+outFile); //$NON-NLS-1$
                }
    		}
    		if (ps == null)
    		    ps= System.out;
   	        ps.println("<html><body>"); //$NON-NLS-1$
            ps.println("Scenario: " + t.getScenarioName()+"<br><br>"); //$NON-NLS-1$ //$NON-NLS-2$
            ps.println("<table>"); //$NON-NLS-1$ //$NON-NLS-2$

            Report r= new Report(2);
            
            String[] timeSeriesLabels= t.getTimeSeriesLabels();
            r.addCell("<tr><td>Builds:</td>"); //$NON-NLS-1$
            for (int j= 0; j < timeSeriesLabels.length; j++)
                r.addCellRight("<td>"+timeSeriesLabels[j]+"</td>");
            ps.print("</tr>");
            r.nextRow();
                        
            Dim[] dimensions= t.getDimensions();
            for (int i= 0; i < dimensions.length; i++) {
                Dim dim= dimensions[i];
                r.addCell("<tr><td><a href=\""+t.getScenarioName().replace('#','.')+"_"+dim.getName()+".jpeg\">"+dim.getName() + ':'+"</a></td>");
                
                TimeSeries ts= t.getTimeSeries(dim);
                int n= ts.getLength();
                for (int j= 0; j < n; j++) {
                    String stddev= " [" + dim.getDisplayValue(ts.getStddev(j)) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
                    r.addCellRight("<td>"+dim.getDisplayValue(ts.getValue(j)) + stddev+"</td>");
                }
                ps.println("</tr>"); //$NON-NLS-1$ //$NON-NLS-2$            
                r.nextRow();
            }
            r.print(ps);
            ps.println();      
            ps.println("</table><br><br>");
            ps.println("</body></html>"); //$NON-NLS-1$
            if (ps != System.out)
                ps.close();
        }

    }
}
