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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.SummaryEntry;
import org.eclipse.test.internal.performance.db.TimeSeries;
import org.eclipse.test.internal.performance.db.Variations;


public class FingerPrint {
    
    private static final int GRAPH_WIDTH= 800;

    String outputDirectory;
    String referenceBuildId;
    String thisBuildID;
    String config;

    public FingerPrint() {
    }

    public FingerPrint(String config, String reference, String thisBuildId, String outputDir) {
        this();
        referenceBuildId= reference;
        this.config= "relengbuildwin2";
        thisBuildID= thisBuildId;
        outputDirectory= outputDir;
        
    }
    public static void main(String args[]) {
        FingerPrint main= new FingerPrint("relengbuildwin2",args[0],args[1],args[2]);  
        main.run();
    }

    public void run() {
        new File(outputDirectory).mkdirs();
        
        BarGraph bar= new BarGraph("Performance of " + thisBuildID + " relative to " + referenceBuildId);
                
        Variations variations= new Variations();
        variations.put(PerformanceTestPlugin.CONFIG, config);
        variations.put(PerformanceTestPlugin.BUILD, thisBuildID);
        // only return summaries for "org.eclipse.jdt.text"; pass a null for all global scenarios
      //  String scenarioPrefix= "org.eclipse.jdt.%";	//$NON-NLS-1$
        SummaryEntry[] entries= DB.querySummaries(variations,null);
        if (entries != null) {
            for (int i= 0; i < entries.length; i++) {
                SummaryEntry se= entries[i];
                add(bar, se.shortName, new Dim[] { se.dimension }, se.scenarioName);    
            }
        }
        
        String outName= "FP_" + referenceBuildId + '_' + thisBuildID;
        save(bar, outputDirectory + '/' + outName);
        //show(bar);
        
        String areas= bar.getAreas();
        if (areas != null) {
	        try {
	            PrintStream os= new PrintStream(new FileOutputStream(outputDirectory + '/' + outName + ".html"));
	            os.println("<html><body>");
	            os.println("<img src=\"performance/" + outName + ".jpeg\" usemap=\"#" + outName + "\">");
	            os.println("<map name=\"" + outName + "\">");
	            os.println(areas);
	            os.println("</map>");
	            os.println("</body></html>");
	            os.close();
	        } catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }
        }
    }
    
    private void add(BarGraph bar, String name, Dim[] dims, String scenarioName) {
        
        String refData= "";
        Variations v= new Variations();
        v.put(PerformanceTestPlugin.CONFIG, config);
        Scenario scenario= DB.getScenarioSeries(scenarioName, v, PerformanceTestPlugin.BUILD, referenceBuildId, thisBuildID, dims);
        String[] timeSeriesLabels= scenario.getTimeSeriesLabels();
        if (timeSeriesLabels.length == 2) {
            // we mark the label with a '*' or '†' to indicate that no data was available for the specified builds
            if (!timeSeriesLabels[0].equals(referenceBuildId)) {
                name= '*' + name;
                refData= " (" + timeSeriesLabels[0] + ")";
            } else if (!timeSeriesLabels[1].equals(thisBuildID)) {
                name= '†' + name;
                refData= " (" + timeSeriesLabels[1] + ")";
            }
        }
        
        for (int i= 0; i < dims.length; i++) {
            TimeSeries timeSeries= scenario.getTimeSeries(dims[i]);
	        int l= timeSeries.getLength();
	        if (l >= 1) {
	            double percent= 0.0;
	            if (l > 1) {
	                double ref= timeSeries.getValue(0);
	                double val= timeSeries.getValue(1);
	            	percent= 100.0 - ((val / ref) * 100.0);
	            }
	            if (Math.abs(percent) < 200) {
	                String n= name + " (" + dims[i].getName() + ")" + refData;
	                bar.addItem(n, percent,"http://download.eclipse.org/downloads/"+thisBuildID.substring(0,1)+"-scenarios/"+scenarioName.replace('#','.').replace(':','_').replace('\\','_')+".html"); //$NON-NLS-1$ //$NON-NLS-2$
	            }
	        }
        }
        //scenario.dump(System.out);
	         
    }

    private void save(BarGraph bar, String output) {

        Display display= Display.getDefault();
        
        int height= bar.getHeight();
        Image image= new Image(display, GRAPH_WIDTH, height);
        
        GC gc= new GC(image);
        bar.paint(display, GRAPH_WIDTH, height, gc);
        gc.dispose();

        ImageLoader il= new ImageLoader();
        il.data= new ImageData[] { image.getImageData()};

        OutputStream out= null;
        try {
            out= new BufferedOutputStream(new FileOutputStream(output + ".jpeg")); //$NON-NLS-1$
            //System.out.println("writing: " + output);
            il.save(out, SWT.IMAGE_JPEG);
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
    
    /*
     * Displays bar graph in window
     */
    private void show(final BarGraph bar) {
        Display display= new Display();
        
        Shell shell= new Shell(display);
        shell.setLayout(new FillLayout());
        
        final Canvas canvas= new Canvas(shell, SWT.NO_BACKGROUND);
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Point s= canvas.getSize();
                bar.paint(canvas.getDisplay(), s.x, s.y, e.gc);
            }
        });
        
        shell.open();

        while (!shell.isDisposed())
            if (!display.readAndDispatch())
                display.sleep();
            
        display.dispose();
    }
}
