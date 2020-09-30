/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.test.internal.performance.results.utils;

/**
 * Interface to define all constants used for performances.
 */
public interface IPerformancesConstants {
    String PLUGIN_ID = "org.eclipse.test.performance.ui"; //$NON-NLS-1$

	String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	// State constants
    String PRE_FILTER_BASELINE_BUILDS = PREFIX + "filter.baseline.builds"; //$NON-NLS-1$
    String PRE_FULL_LINE_SELECTION  = PREFIX + "full.line.selection"; //$NON-NLS-1$
    String PRE_WRITE_RESULTS_DIR = PREFIX + "write.results.dir"; //$NON-NLS-1$

	// Preference constants
    String PRE_ECLIPSE_VERSION = PREFIX + "eclipse.version"; //$NON-NLS-1$
    String PRE_DATABASE_CONNECTION = PREFIX + "database.connection"; //$NON-NLS-1$
    String PRE_DATABASE_LOCAL = PREFIX + "local"; //$NON-NLS-1$
    String PRE_DATABASE_LOCATION = PREFIX + "database.location"; //$NON-NLS-1$
    String PRE_LOCAL_DATA_DIR = PREFIX + "local.data.dir"; //$NON-NLS-1$
    String PRE_RESULTS_GENERATION_DIR = PREFIX + "results.generation.dir"; //$NON-NLS-1$
    String PRE_CONFIG_DESCRIPTOR_NAME = PREFIX + "config.descriptor.name"; //$NON-NLS-1$
    String PRE_CONFIG_DESCRIPTOR_DESCRIPTION = PREFIX + "config.descriptor.description"; //$NON-NLS-1$
    String PRE_DEFAULT_DIMENSION = PREFIX + "default.dimension"; //$NON-NLS-1$
    String PRE_RESULTS_DIMENSION = PREFIX + "results.dimension"; //$NON-NLS-1$
    String PRE_MILESTONE_BUILDS = PREFIX + "milestone.builds"; //$NON-NLS-1$
    String PRE_STATUS_COMMENT_PREFIX = PREFIX + "status.comment"; //$NON-NLS-1$
    String PRE_FILTER_ADVANCED_SCENARIOS = PREFIX + "filter.non.fingerprints.scenarios"; //$NON-NLS-1$
    String PRE_FILTER_OLD_BUILDS = PREFIX + "filter.non.milestones.builds"; //$NON-NLS-1$
    String PRE_FILTER_NIGHTLY_BUILDS = PREFIX + "filter.nightly.builds"; //$NON-NLS-1$

	// Default values
	String DATABASE_NAME_PREFIX = "perfDb";
	String NETWORK_DATABASE_LOCATION = "//shared/eclipse/databases/eclipseDB45/perfDb45:1528";
	boolean DEFAULT_FILTER_ADVANCED_SCENARIOS = true;
	boolean DEFAULT_FILTER_OLD_BUILDS = false;
	boolean DEFAULT_FILTER_NIGHTLY_BUILDS = false;
	boolean DEFAULT_DATABASE_CONNECTION = false;
	boolean DEFAULT_DATABASE_LOCAL = false;

	// Status
    String PRE_WRITE_STATUS = PREFIX + "write.status"; //$NON-NLS-1$
	int STATUS_BUILDS_NUMBER_MASK= 0x00FF;
	int DEFAULT_BUILDS_NUMBER = 3;
	int STATUS_VALUES = 0x0100;
	int STATUS_ERROR_NONE = 0x0200;
	int STATUS_ERROR_NOTICEABLE = 0x0400;
	int STATUS_ERROR_SUSPICIOUS = 0x0600;
	int STATUS_ERROR_WEIRD = 0x0800;
	int STATUS_ERROR_INVALID = 0x0A00;
	int STATUS_ERROR_LEVEL_MASK = 0x0E00;
	int STATUS_SMALL_VALUE_BUILD = 0x1000;
	int STATUS_SMALL_VALUE_DELTA = 0x2000;
	int STATUS_SMALL_VALUE_MASK = 0x3000;
	int STATUS_STATISTICS_ERRATIC = 0x4000;
	int STATUS_STATISTICS_UNSTABLE = 0x8000;
	int STATUS_STATISTICS_MASK = 0xC000;
	int DEFAULT_WRITE_STATUS = STATUS_ERROR_NONE | DEFAULT_BUILDS_NUMBER;

	// Comparison
    String PRE_COMPARISON_THRESHOLD_FAILURE = PREFIX + "comparison.threshold.failure"; //$NON-NLS-1$
	int DEFAULT_COMPARISON_THRESHOLD_FAILURE = 10;
    String PRE_COMPARISON_THRESHOLD_ERROR = PREFIX + "comparison.threshold.error"; //$NON-NLS-1$
	int DEFAULT_COMPARISON_THRESHOLD_ERROR = 3;
    String PRE_COMPARISON_THRESHOLD_IMPROVEMENT = PREFIX + "comparison.threshold.imporvement"; //$NON-NLS-1$
	int DEFAULT_COMPARISON_THRESHOLD_IMPROVEMENT = 10;

	int ECLIPSE_DEVELOPMENT_VERSION = 418;
	int DEFAULT_ECLIPSE_VERSION = ECLIPSE_DEVELOPMENT_VERSION;
	int ECLIPSE_MAINTENANCE_VERSION = 417;

}
