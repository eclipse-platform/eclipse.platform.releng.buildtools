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

    public static void main(String[] args) {
    	run("org.eclipse.ant");
    //	run("org.eclipse.core.tests.runtime.perf.StartupTest.testApplicationStartup");
    //	run("org.eclipse.osgi");
    // 	run("org.eclipse.jdt.debug");
    // 	run("org.eclipse.jdt.text");
    // 	run("org.eclipse.jdt.ui");
     // run("org.eclipse.swt");
     //  	run("org.eclipse.team");
     //  	run("org.eclipse.ui");

    }
    

    public static  void run (String scenario) {
		

		String outFile= null;
		outFile= "out/"+scenario+".html";	//$NON-NLS-1$
		PrintStream ps= null;

        // get all Scenarios 
        Dim[] qd= null; // new Dim[] { InternalDimensions.CPU_TIME };
        
        Scenario[] scenarios= DB.queryScenarios("%", "I%", scenario+"%", qd); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    //    Main grapher =new Main(scenarios,"out");
 
 
        
        for (int s= 0; s < scenarios.length; s++) {
            Scenario t= scenarios[s];
            outFile="out/"+t.getScenarioName().replace('#','.')+".html";
    		if (outFile != null) {
    		    try {
                    ps= new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
                } catch (FileNotFoundException e) {
                    System.err.println("can't create output file"); //$NON-NLS-1$
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
