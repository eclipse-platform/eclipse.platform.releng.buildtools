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
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
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
import org.eclipse.test.internal.performance.InternalDimensions;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.TimeSeries;


public class FingerPrint extends Task {
    
    private static final int GRAPH_WIDTH= 800;

    String outputDirectory;
    String referenceBuildId;
    String thisBuildID;
    String config;

    public FingerPrint() {
    }

    public static void main(String args[]) {

        FingerPrint main= new FingerPrint();

        main.referenceBuildId= "args[0]";
        main.config= "relengbuildwin2";
        main.thisBuildID= "args[1]";
        main.outputDirectory= "args[2]";
       
        main.execute();
    }

    public void execute() throws BuildException {

        new File(outputDirectory).mkdirs();
        
        BarGraph bar= new BarGraph("Performance of " + thisBuildID + " relative to " + referenceBuildId);
        
        Dim[] stdDims= new Dim[] { InternalDimensions.CPU_TIME };
        Dim[] elapsedDim= new Dim[] { InternalDimensions.ELAPSED_PROCESS };

        add(bar, "Startup Workbench", elapsedDim, "org.eclipse.core.tests.runtime.perf.StartupTest.testApplicationStartup");
        add(bar, "UIStartup Workbench", elapsedDim, "org.eclipse.core.tests.runtime.perf.UIStartupTest.testApplicationStartup");
        
        add(bar, "Move Comp Unit", stdDims, "org.eclipse.jdt.ui.tests.refactoring.reorg.MoveCompilationUnitPerfTests1#test_1000_10()");
        add(bar, "Open Package Explorer", stdDims, "org.eclipse.jdt.ui.tests.performance.views.PackageExplorerPerfTest#testOpen()");
        add(bar, "Open Ant Editor", stdDims, "org.eclipse.ant.tests.ui.editor.performance.OpenAntEditorTest#testOpenAntEditor1()");
        add(bar, "Revert Java Editor", stdDims, "org.eclipse.jdt.text.tests.performance.RevertJavaEditorTest#testRevertJavaEditor()");
        add(bar, "Java Perspective Switch", stdDims, "org.eclipse.ui.tests.performance.PerspectiveSwitchTest#testPerspectiveSwitch:org.eclipse.jdt.ui.JavaPerspective,org.eclipse.ui.tests.util.EmptyPerspective,editor 1.perf_basic()");
        add(bar, "Open Close Editors", stdDims, "org.eclipse.ui.tests.performance.OpenCloseEditorTest#testOpenAndCloseEditors:java()");
        add(bar, "Run and Shutdown Workbench", stdDims, "org.eclipse.ui.tests.rcp.performance.PlatformUIPerfTest#testRunAndShutdownWorkbench()");
        add(bar, "Rename Java Type", stdDims, "org.eclipse.jdt.ui.tests.refactoring.reorg.RenameTypePerfTests1#test_1000_10()");
        add(bar, "Scroll Linewise", stdDims, "org.eclipse.jdt.text.tests.performance.ScrollTextEditorTest#testScrollTextEditorLineWise2()");
        add(bar, "Open Quick Outline", stdDims, "org.eclipse.jdt.text.tests.performance.OpenQuickOutlineTest#testOpenQuickOutline1()-warm");
        add(bar, "Classpath Cycle Detection", stdDims, "org.eclipse.jdt.core.tests.model.ClasspathTests#testPerfDenseCycleDetection1()");
        
        save(bar, outputDirectory + "/FP_" + referenceBuildId + "_" + thisBuildID);
        //show(bar);
    }
    
    private void add(BarGraph bar, String name, Dim[] dims, String scenarioName) {
        
        Scenario scenario= DB.queryScenario(config, new String[] { referenceBuildId, thisBuildID }, scenarioName);
        
        for (int i= 0; i < dims.length; i++) {
	        TimeSeries timeSeries= scenario.getTimeSeries(dims[i]);
	        if (timeSeries.getLength() > 1) {
	            double ref= timeSeries.getValue(0);
	            double val= timeSeries.getValue(1);
	            
	            double percent= 100.0 - ((val / ref) * 100.0);
	            
	            bar.addItem(name + " (" + dims[i].getName() + ")", percent); //$NON-NLS-1$ //$NON-NLS-2$
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
