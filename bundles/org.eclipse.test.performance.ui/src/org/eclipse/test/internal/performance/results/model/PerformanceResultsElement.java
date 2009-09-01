/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.internal.performance.results.model;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.test.internal.performance.results.db.*;
import org.eclipse.test.internal.performance.results.utils.Util;

public class PerformanceResultsElement extends ResultsElement {

// Singleton pattern
public static PerformanceResultsElement PERF_RESULTS_MODEL = new PerformanceResultsElement();

	String[] buildNames;
	boolean fingerprints = true;

public PerformanceResultsElement() {
	super();
}

ResultsElement createChild(AbstractResults testResults) {
	return new ComponentResultsElement(testResults, this);
}

public String[] getBaselines() {
	String[] builds = DB_Results.getBuilds();
	int length = builds.length;
	String[] baselines = new String[length];
	int count = 0;
	for (int i=0; i<length; i++) {
		if (builds[i].startsWith("R-")) {
			baselines[count++] = builds[i];
		}
	}
	if (count < length) {
		System.arraycopy(baselines, 0, baselines = new String [count], 0, count);
	}
	return baselines;
}

String[] getBuildNames() {
	if (this.buildNames == null) {
		this.buildNames = this.results == null ? new String[0] : getPerformanceResults().getAllBuildNames();
	}
	return this.buildNames;
}

public Object[] getBuilds() {
	String[] builds = DB_Results.getBuilds();
	int length = builds.length;
	BuildResultsElement[] elements = new BuildResultsElement[length];
	for (int i=0; i<length; i++) {
		elements[i] = new BuildResultsElement(builds[i], this);
	}
	return elements;
}

public String[] getComponents() {
	if (this.results == null) {
		String[] components = DB_Results.getComponents();
		int length = components.length;
		if (length == 0) {
			DB_Results.queryAllScenarios();
			components = DB_Results.getComponents();
		}
		return components;
	}
	return ((PerformanceResults) this.results).getComponents();
}

public Object[] getElements() {
	if (this.results == null) {
		String[] components = getComponents();
		int length = components.length;
		ComponentResultsElement[] elements = new ComponentResultsElement[length];
		for (int i=0; i<length; i++) {
			elements[i] = new ComponentResultsElement(components[i], this);
		}
		return elements;
	}
	return getChildren(null);
}

public PerformanceResults getPerformanceResults() {
	return (PerformanceResults) this.results;
}

boolean hasRead(BuildResultsElement buildResultsElement) {
	String[] builds = getBuildNames();
	if (Arrays.binarySearch(builds, buildResultsElement.getName(),
		new Comparator() {
		public int compare(Object o1, Object o2) {
	        String s1 = (String) o1;
	        String s2 = (String) o2;
	        return Util.getBuildDate(s1).compareTo(Util.getBuildDate(s2));
	    }
	}) < 0) {
		return false;
	}
	return true;
}

public void readLocal(File dataDir, IProgressMonitor monitor) {
	this.results = new PerformanceResults(null, null, null, System.out);
	reset();
	getPerformanceResults().readLocal(dataDir, monitor);
}

public void reset() {
	this.children = null;
	this.buildNames = null;
}

public void updateBuild(String buildName, boolean force, File dataDir, IProgressMonitor monitor) {
	if (this.results == null) {
//		throw new IllegalArgumentException("Unexpected null results!");
		this.results = new PerformanceResults(buildName, null, null, System.out);
		reset();
	}
	getPerformanceResults().updateBuild(buildName, force, dataDir, monitor);
}

public void updateBuilds(String[] builds, boolean force, File dataDir, IProgressMonitor monitor) {
	if (this.results == null) {
//		throw new IllegalArgumentException("Unexpected null results!");
		this.results = new PerformanceResults(null, null, null, System.out);
		reset();
	}
	getPerformanceResults().updateBuilds(builds, force, dataDir, monitor);
}

/**
 * Set whether only fingerprints should be taken into account or not.
 *
 * @param fingerprints
 */
public void setFingerprints(boolean fingerprints) {
	this.fingerprints = fingerprints;
	resetStatus();
}

}
