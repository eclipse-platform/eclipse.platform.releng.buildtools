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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;


public class BarGraph {

    private static final int MARGIN= 5;		// margin on all four sides
    private static final int BARHEIGHT= 8;	// height of bar
    private static final int GAP= 10;		// gap between bars
    private static final int TGAP= 5; 		// gap between lines and labels
    
    private StringBuffer fAreaBuffer;
    
    private static class BarItem {

        String title;
        double value;
        String url;

        BarItem(String t, double v, String u) {
            title= t;
            value= v;
            url= u;
        }
    }

    private String fTitle;
    private List fItems;
        
   
    BarGraph(String title) {
        fTitle= title;
        fItems= new ArrayList();
    }
    
    public void addItem(String name, double value) {
        fItems.add(new BarItem(name, value, null));
    }

    public void addItem(String name, double value, String url) {
        fItems.add(new BarItem(name, value, url));
    }

    public int getHeight() {
        int n= fItems.size();
        int textHeight= 16;
        int titleHeight= 0;
        if (fTitle != null)
            titleHeight= textHeight + GAP;
        return MARGIN + titleHeight + n*(GAP+BARHEIGHT) + GAP+textHeight + MARGIN;
    }

    public void paint(Display display, int width, int height, GC gc) {
        
		NumberFormat nf= NumberFormat.getInstance();
		nf.setMaximumFractionDigits(1);

		BarItem[] bars= (BarItem[]) fItems.toArray(new BarItem[fItems.size()]);
        
        // draw white background
        Color bg= display.getSystemColor(SWT.COLOR_WHITE);
        gc.setBackground(bg);
        gc.fillRectangle(0, 0, width, height);

        int maxNameLength= 0;
        for (int i= 0; i < bars.length; i++) {
            Point es= gc.stringExtent(bars[i].title);
            maxNameLength= Math.max(maxNameLength, es.x);
        }

        int w= width - maxNameLength-TGAP-2*MARGIN;
        
        Color fg= display.getSystemColor(SWT.COLOR_BLACK);
        
        int vstart= 0;	// start rows here
        if (fTitle != null) {
            vstart= gc.stringExtent(fTitle).y + GAP;
            gc.drawString(fTitle, MARGIN, MARGIN, true);	// draw title left aligned
        }
        
        int center= MARGIN+w/2;
        int w2= w/2-gc.stringExtent("-99.9").x-TGAP;	// reserve space //$NON-NLS-1$

        // determine maximum of values
        double max= 0.0;
        for (int i= 0; i < bars.length; i++) 
            max= Math.max(max, Math.abs(bars[i].value));
        
        double d;
        if (max > 400.0) {
            d= 200;
        } else if (max > 200.0) {
            d= 100;
        } else if (max > 100.0) {
            d= 50;
        } else if (max > 50) {
            d= 25;
        } else if (max > 25) {
            d= 10;
        } else if (max > 10) {
            d= 5;
        } else if (max > 5) {
            d= 2.5;
        } else {
            d= 1.0;
        }
        
        // draw striped background
        int y= MARGIN+vstart;
        Color lightblue= new Color(display, 237, 243, 254);
        gc.setBackground(lightblue);
        for (int i= 0; i < bars.length; i++)
            if (i % 2 == 0)
                gc.fillRectangle(0, y+i*(BARHEIGHT+GAP), width, BARHEIGHT+GAP);
        lightblue.dispose();
        
        // draw grid
        int yy= y + bars.length*(BARHEIGHT+GAP);
        gc.drawLine(center, y, center, yy+TGAP);
        Color grey= display.getSystemColor(SWT.COLOR_GRAY);
        for (int i= 1; d*i < max; i++) {
            
            double xx= d*i;
            int x= (int)((xx/max)*w2);
            
            gc.setForeground(grey);
            gc.drawLine(center-x, y, center-x, yy+TGAP);
            gc.drawLine(center+x, y, center+x, yy+TGAP);

            gc.setForeground(fg);

            String s3= Double.toString(-xx) + "%"; //$NON-NLS-1$
            Point es3= gc.stringExtent(s3);
            gc.drawString(s3, center-x-es3.x/2, yy+TGAP, true);
            
            String s4= Double.toString(xx) + "%"; //$NON-NLS-1$
            Point es4= gc.stringExtent(s4);
            gc.drawString(s4, center+x-es4.x/2, yy+TGAP, true);
        }
        gc.drawLine(0, yy, w, yy);
        
        // link color
        Color blue= display.getSystemColor(SWT.COLOR_BLUE);        
        // draw bars
        Color green= display.getSystemColor(SWT.COLOR_GREEN);
        Color red= display.getSystemColor(SWT.COLOR_RED);
        for (int i= 0; i < bars.length; i++) {
            
            double delta= bars[i].value;
            int barLength= (int)(delta/max*w2);                
                              
            if (delta > 0.0)
                gc.setBackground(green);
            else
                gc.setBackground(red);
            gc.fillRectangle(center, y+(GAP/2), barLength, BARHEIGHT);
            gc.drawRectangle(center, y+(GAP/2), barLength, BARHEIGHT);
            
            String label= nf.format(delta);
            Point labelExtent= gc.stringExtent(label);
            int labelxpos= center+barLength;
            int labelvpos= y+(BARHEIGHT+GAP-labelExtent.y)/2;
            if (delta > 0.0) {
                gc.drawString(label, labelxpos+TGAP, labelvpos, true);
            } else {
                gc.drawString(label, labelxpos-TGAP-labelExtent.x, labelvpos, true);
            }
            
            int x= MARGIN+w+TGAP;
            String title= bars[i].title;
            boolean hasURL= bars[i].url != null;
            Color oldfg= gc.getForeground();
            if (hasURL) {
                gc.setForeground(blue);
                Point e= gc.stringExtent(title);
                gc.drawLine(x, labelvpos+e.y-1, x+e.x, labelvpos+e.y-1);
            }
            gc.drawString(title, x, labelvpos, true);
            if (hasURL)
                gc.setForeground(oldfg);            
                                
            int y0= y;
            y+= BARHEIGHT+GAP;            
            
            if (hasURL) {
                if (fAreaBuffer == null)
                    fAreaBuffer= new StringBuffer();
                fAreaBuffer.append("<area shape=\"RECT\" coords=\"0,"+y0+','+width+','+y+"\" href=\""+ bars[i].url +"\">\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }
    
    public String getAreas() {
        if (fAreaBuffer != null) {
            String s= fAreaBuffer.toString();
            fAreaBuffer= null;
            return s;
        }
        return null;
    }
}
