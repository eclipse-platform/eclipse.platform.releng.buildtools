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
package org.eclipse.test.internal.performance.results.ui;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.results.db.DB_Results;
import org.eclipse.test.internal.performance.results.utils.IPerformancesConstants;
import org.eclipse.test.internal.performance.results.utils.Util;
import org.eclipse.test.performance.ui.UiPlugin;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Defines the 'Performances' preferences page.
 */
public class PerformanceResultsPreferencePage extends PreferencePage
	implements IWorkbenchPreferencePage, SelectionListener, ModifyListener, IPerformancesConstants {

//	private Button maintenanceVersion;
//	private Button developmentVersion;
//	private CCombo databaseLocation;
	private CCombo databaseLocationCombo;
	private Button localButton;
	private Button relengButton;
	private Table configDescriptorsTable;
	private CCombo defaultDimensionCombo;
	private List resultsDimensionsList;
	private CCombo milestonesCombo;

/**
 * Create a text field specific for this application
 *
 * @param parent
 *            the parent of the new text field
 * @return the new text field
 */
private CCombo createCombo(Composite parent) {
	CCombo combo= new CCombo(parent, SWT.BORDER);
	combo.addModifyListener(this);
	GridData data = new GridData();
	data.horizontalSpan = 2;
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.verticalAlignment = GridData.CENTER;
	data.grabExcessVerticalSpace = false;
	combo.setLayoutData(data);
	return combo;
}


/**
 * Creates composite control and sets the default layout data.
 *
 * @param parent
 *            the parent of the new composite
 * @param numColumns
 *            the number of columns for the new composite
 * @return the newly-created coposite
 */
private Composite createComposite(Composite parent, int numColumns) {
	Composite composite = new Composite(parent, SWT.NULL);

	// GridLayout
	GridLayout layout = new GridLayout();
	layout.numColumns = numColumns;
	composite.setLayout(layout);

	// GridData
	GridData data = new GridData();
	data.verticalAlignment = GridData.FILL;
	data.horizontalAlignment = GridData.FILL;
	composite.setLayoutData(data);
	return composite;
}

/**
 * (non-Javadoc) Method declared on PreferencePage
 */
protected Control createContents(Composite parent) {

	// Eclipse version choice
//	Composite composite_eclipseVersion = createComposite(parent, 2);
//	createLabel(composite_eclipseVersion, "Eclipse version");
//	Composite composite_versionChoice = createComposite(composite_eclipseVersion, 2);
//	this.maintenanceVersion = createRadioButton(composite_versionChoice, ECLIPSE_MAINTENANCE_VERSION);
//	this.developmentVersion = createRadioButton(composite_versionChoice, ECLIPSE_DEVELOPMENT_VERSION);

	// Database location
	Composite compositeDatabaseLocation = createComposite(parent, 5);
	createLabel(compositeDatabaseLocation, "Database location", false);
	this.databaseLocationCombo = createCombo(compositeDatabaseLocation);
	this.databaseLocationCombo.setEditable(false);
    this.localButton = createPushButton(compositeDatabaseLocation, "Local");
    this.relengButton = createPushButton(compositeDatabaseLocation, "Releng");

	// Milestones
	Composite compositeMilestones = createComposite(parent, 3);
	createLabel(compositeMilestones, "Milestones", false);
	this.milestonesCombo = createCombo(compositeMilestones);
	this.milestonesCombo.setToolTipText("Enter the date of the milestone as yyyymmddHHMM");

	// Dimension group layout
	// TODO Put all the dimensions preferences in a group
	// Currently that does not work, when using the group, none of the controls
	// defines below are shown in the page???
//	Composite compositeDimensions = createComposite(parent, 4);
//	Group dimensionsGroup = createGroup(parent, "Dimensions");
//	dimensionsGroup.pack();

	// Default dimension layout
	Composite compositeDefaultDimension = createComposite(parent, 3);
	createLabel(compositeDefaultDimension, "Default dimension: ", false);
	this.defaultDimensionCombo = createCombo(compositeDefaultDimension);
	this.defaultDimensionCombo.setEditable(false);

	// Results dimensions layout
	Composite compositeResultsDimensions = createComposite(parent, 3);
	createLabel(compositeResultsDimensions, "Results dimensions: ", true/*beginning*/);
	this.resultsDimensionsList = createList(compositeResultsDimensions);

	// Config descriptors layout
	Composite compositeConfigDescriptors = createComposite(parent, 3);
	createLabel(compositeConfigDescriptors, "Config descriptors: ", false);
	this.configDescriptorsTable = createTable(compositeConfigDescriptors);
	TableColumn firstColumn = new TableColumn(this.configDescriptorsTable, SWT.LEFT);
	firstColumn.setText ("Name");
	firstColumn.setWidth(50);
	TableColumn secondColumn = new TableColumn(this.configDescriptorsTable, SWT.FILL | SWT.LEFT);
	secondColumn.setText ("Description");
	secondColumn.setWidth(300);

	// init values
	initializeValues();

	// font = null;
	Composite contents = new Composite(parent, SWT.NULL);
	contents.pack(true);
	return contents;
}

/*
 * Utility method that creates a label instance and sets the default layout
 * data.
 *
 * @param parent
 *            the parent for the new label
 * @param text
 *            the text for the new label
 * @return the new label
 *
private Group createGroup(Composite parent, String text) {
	Group group = new Group(parent, SWT.LEFT);
	group.setText(text);
	GridData data = new GridData();
	data.horizontalSpan = 4;
	data.horizontalAlignment = GridData.FILL;
	data.verticalSpan = 4;
	data.verticalAlignment = GridData.FILL;
	group.setLayoutData(data);
	return group;
}
*/

/**
 * Utility method that creates a label instance and sets the default layout
 * data.
 *
 * @param parent
 *            the parent for the new label
 * @param text
 *            the text for the new label
 * @param beginning TODO
 * @return the new label
 */
private Label createLabel(Composite parent, String text, boolean beginning) {
	Label label = new Label(parent, SWT.BEGINNING|SWT.LEFT);
	label.setText(text);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.verticalAlignment = beginning ? GridData.BEGINNING : GridData.CENTER;
	label.setLayoutData(data);
	return label;
}

/**
 * Create a text field specific for this application
 *
 * @param parent
 *            the parent of the new text field
 * @return the new text field
 */
private List createList(Composite parent) {
	List list = new List(parent, SWT.MULTI | SWT.BORDER);
	list.addSelectionListener(this);
	GridData data = new GridData();
	data.horizontalSpan = 2;
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.verticalAlignment = GridData.CENTER;
	data.grabExcessVerticalSpace = false;
	list.setLayoutData(data);
	return list;
}

/**
 * Utility method that creates a push button instance and sets the default
 * layout data.
 *
 * @param parent
 *            the parent for the new button
 * @param label
 *            the label for the new button
 * @return the newly-created button
 */
private Button createPushButton(Composite parent, String label) {
	Button button = new Button(parent, SWT.PUSH);
	button.setText(label);
	button.addSelectionListener(this);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	button.setLayoutData(data);
	return button;
}

/*
 * Utility method that creates a radio button instance and sets the default
 * layout data.
 *
 * @param parent
 *            the parent for the new button
 * @param label
 *            the label for the new button
 * @return the newly-created button
 *
private Button createRadioButton(Composite parent, String label) {
	Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
	button.setText(label);
	button.addSelectionListener(this);
	GridData data = new GridData();
	button.setLayoutData(data);
	return button;
}
*/

/**
 * Create a text field specific for this application
 *
 * @param parent
 *            the parent of the new text field
 * @return the new text field
 */
private Table createTable(Composite parent) {
	Table table = new Table(parent, SWT.BORDER);
	table.setLinesVisible (true);
	table.setHeaderVisible (true);
	GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
	gridData.heightHint = 150;
	table.setLayoutData(gridData);
	return table;
}

/*
 * Create a text field specific for this application
 *
 * @param parent
 *            the parent of the new text field
 * @return the new text field
 *
private Text createTextField(Composite parent) {
	Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
	text.addModifyListener(this);
	GridData data = new GridData();
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.verticalAlignment = GridData.CENTER;
	data.grabExcessVerticalSpace = false;
	text.setLayoutData(data);
	return text;
}

/**
 * The <code>ReadmePreferencePage</code> implementation of this
 * <code>PreferencePage</code> method returns preference store that belongs to
 * the our plugin. This is important because we want to store our preferences
 * separately from the workbench.
 */
protected IPreferenceStore doGetPreferenceStore() {
	return UiPlugin.getDefault().getPreferenceStore();
}

/*
 * Get the directory path using the given location as default.
 */
private String getDirectoryPath(String location) {
	DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
	dialog.setText(DB_Results.getDbTitle());
	dialog.setMessage("Select local database directory:");
	dialog.setFilterPath(location);
	String path = dialog.open();
	if (path != null) {
		File dir = new File(path);
		if (dir.exists() && dir.isDirectory()) {
			return dir.getAbsolutePath();
		}
	}
	return null;
}

/*
 * (non-Javadoc) Method declared on IWorkbenchPreferencePage
 */
public void init(IWorkbench workbench) {
	// do nothing
}

/*
 * Init he contents of the dimensions list controls.
 */
void initDimensionsLists() {
	// Dimensions lists
	java.util.List dimensions = PerformanceTestPlugin.getDimensions();
	Iterator names = dimensions.iterator();
	while (names.hasNext()) {
		String name = (String) names.next();
		this.defaultDimensionCombo.add(name);
		this.resultsDimensionsList.add(name);
	}
}

/**
 * Initializes states of the controls using default values in the preference
 * store.
 */
private void initializeDefaults() {
	IPreferenceStore store = getPreferenceStore();

	// Init eclipse version
//	this.maintenanceVersion.setSelection(false);
//	this.developmentVersion.setSelection(false);
//	String version = dataDir.substring(index+1);
//	if (version.equals(ECLIPSE_MAINTENANCE_VERSION)) {
//		this.maintenanceVersion.setSelection(true);
//	} else {
//		this.developmentVersion.setSelection(true);
//	}

	// Init default database location
	String location = store.getDefaultString(PRE_DATABASE_LOCATION);
	this.databaseLocationCombo.setText(location);
	int count = this.databaseLocationCombo.getItemCount();
	for (int i=0; i<count; i++) {
		String item = this.databaseLocationCombo.getItem(i);
		if (item.equals(location)) {
			this.databaseLocationCombo.remove(i);
			break;
		}
	}
	this.databaseLocationCombo.add(location, 0);

	// Milestones
	this.milestonesCombo.removeAll();
	final String databaseLocation = this.databaseLocationCombo.getText();
	char last = databaseLocation.charAt(databaseLocation.length()-1);
	String version = last == '5' ? ".v35." : ".v36.";
	String milestone = store.getDefaultString(PRE_MILESTONE_BUILDS + version + "0");
	int index = 0;
	while (milestone != null && milestone.length() > 0) {
		this.milestonesCombo.add(milestone);
		milestone = store.getDefaultString(PRE_MILESTONE_BUILDS + version + index);
	}

	// Init default default dimension
	String defaultDimension = store.getDefaultString(PRE_DEFAULT_DIMENSION);
	this.defaultDimensionCombo.setText(defaultDimension);

	// Init default generated dimensions
	this.resultsDimensionsList.add(store.getDefaultString(PRE_RESULTS_DIMENSION+".0"));
	this.resultsDimensionsList.add(store.getDefaultString(PRE_RESULTS_DIMENSION+".1"));
}

/**
 * Initializes states of the controls from the preference store.
 */
private void initializeValues() {
	IPreferenceStore store = getPreferenceStore();

	// Init eclipse version
//	String version = dataDir.substring(index+1);
//	if (version.equals(ECLIPSE_MAINTENANCE_VERSION)) {
//		this.maintenanceVersion.setSelection(true);
//	} else {
//		this.developmentVersion.setSelection(true);
//	}

	// Init database location
	this.databaseLocationCombo.removeAll();
	this.databaseLocationCombo.setText(store.getString(PRE_DATABASE_LOCATION));
	for (int i = 0; i < 3; i++) {
		String history = store.getString(PRE_DATABASE_LOCATION + "." + i);
		if (history.length() == 0)
			break;
		this.databaseLocationCombo.add(history);
	}

	// Milestones
	final String databaseLocation = this.databaseLocationCombo.getText();
	char last = databaseLocation.charAt(databaseLocation.length()-1);
	String version = last == '5' ? ".v35." : ".v36.";
	String milestone = store.getString(PRE_MILESTONE_BUILDS + version + "0");
	int index = 0;
	while (milestone != null && milestone.length() > 0) {
		this.milestonesCombo.add(milestone);
		milestone = store.getString(PRE_MILESTONE_BUILDS + version + ++index);
	}

	// Init composite lists
	initDimensionsLists();

	// Init default dimension
	String defaultDimension = store.getString(PRE_DEFAULT_DIMENSION);
	this.defaultDimensionCombo.setText(defaultDimension);

	// Init generated dimensions
	int i = 0;
	int count = this.resultsDimensionsList.getItemCount();
	int[] indices = new int[count];
	int n = 0;
	String resultsDimension = store.getString(PRE_RESULTS_DIMENSION + "." + i++);
	while (resultsDimension.length() > 0) {
		indices[n++] = this.resultsDimensionsList.indexOf(resultsDimension);
		resultsDimension = store.getString(PRE_RESULTS_DIMENSION + "." + i++);
	}
	if (n < count) {
		System.arraycopy(indices, 0, indices = new int[n], 0, n);
	}
	this.resultsDimensionsList.select(indices);

	// Init config descriptors
	this.configDescriptorsTable.clearAll();
	int d = 0;
	String descriptorName = store.getString(PRE_CONFIG_DESCRIPTOR_NAME + "." + d);
	String descriptorDescription = store.getString(PRE_CONFIG_DESCRIPTOR_DESCRIPTION + "." + d++);
	while (descriptorName.length() > 0) {
		TableItem tableItem = new TableItem (this.configDescriptorsTable, SWT.NONE);
		tableItem.setText (0, descriptorName);
		tableItem.setText (1, descriptorDescription);
		descriptorName = store.getString(PRE_CONFIG_DESCRIPTOR_NAME + "." + d);
		descriptorDescription = store.getString(PRE_CONFIG_DESCRIPTOR_DESCRIPTION + "." + d++);
	}
}

/**
 * (non-Javadoc) Method declared on ModifyListener
 */
public void modifyText(ModifyEvent event) {

	// Add default dimension to results if necessary
	if (event.getSource() == this.defaultDimensionCombo) {
		String[] resultsDimensions = this.resultsDimensionsList.getSelection();
		int length = resultsDimensions.length;
		String defaultDimension = this.defaultDimensionCombo.getText();
		for (int i = 0; i < length; i++) {
			if (resultsDimensions[i].equals(defaultDimension)) {
				// Default dim is already set as a results dimension, hence nothing has to be done
				return;
			}
		}
		System.arraycopy(resultsDimensions, 0, resultsDimensions = new String[length + 1], 0, length);
		resultsDimensions[length] = defaultDimension;
		this.resultsDimensionsList.setSelection(resultsDimensions);
	}

	// Add default dimension to results if necessary
	if (event.getSource() == this.milestonesCombo) {

		// Verify the only digits are entered
		String milestoneDate = this.milestonesCombo.getText();
		final int mLength = milestoneDate.length();
		if (mLength > 0 && !Character.isDigit(milestoneDate.charAt(mLength-1))) {
			openMilestoneErrorMessage(milestoneDate);
			return;
		}

		// Do not verify further until a complete milestone date is entered
		if (mLength < 12) return;

		// Verify the digits
		try {
			String str = milestoneDate.substring(0, 4);
			int year = Integer.parseInt(str);
			if (year < 2009 || year > 2020) { // 2020 should be enough!
				MessageDialog.openError(getShell(), DB_Results.getDbTitle(), milestoneDate+": "+str+" is an invalid year, only value between 2009 and 2020 is accepted!");
				return;
			}
			str = milestoneDate.substring(4, 6);
			int month = Integer.parseInt(str);
			if (month <= 0 || month > 12) {
				MessageDialog.openError(getShell(), DB_Results.getDbTitle(), milestoneDate+": "+str+" is an invalid month, it should be only from 01 to 12!");
				return;
			}
			str = milestoneDate.substring(6, 8);
			int day = Integer.parseInt(str);
			if (day <= 0 || day > 31) {
				// TODO improve this verification
				MessageDialog.openError(getShell(), DB_Results.getDbTitle(), milestoneDate+": "+str+" is an invalid day, it should be only from 01 to 31!");
				return;
			}
			str = milestoneDate.substring(8, 10);
			int hour = Integer.parseInt(str);
			if (hour < 0 || hour > 23) {
				MessageDialog.openError(getShell(), DB_Results.getDbTitle(), milestoneDate+": "+str+" is an invalid hour, it should be only from 00 to 23!");
				return;
			}
			str = milestoneDate.substring(10, 12);
			int min = Integer.parseInt(str);
			if (min < 0 || min > 59) {
				MessageDialog.openError(getShell(), DB_Results.getDbTitle(), milestoneDate+": "+str+" is invalid minutes, it should be only from 00 to 59!");
				return;
			}
		}
		catch (NumberFormatException nfe) {
			openMilestoneErrorMessage(milestoneDate);
		}

		// Get combo info
		String[] milestones = this.milestonesCombo.getItems();
		int length = milestones.length;
		String lastMilestone = length == 0 ? null : milestones[length-1];

		// Verify that the added milestone is valid
		final String databaseLocation = this.databaseLocationCombo.getText();
		char version = databaseLocation.charAt(databaseLocation.length()-1);

		// Verify that the milestone follow the last one
		String milestoneName;
		if (lastMilestone == null) {
			// No previous last milestone
			milestoneName = "M1";
		} else {
			// Compare with last milestone
			if (lastMilestone.charAt(0) == 'M') {
				char digit = lastMilestone.charAt(1);
				if (digit == '6') {
					// M6 is the last dvpt milestone
					milestoneName = "RC1";
				} else {
					milestoneName = "M" +((char)(digit+1));
				}
			} else if (lastMilestone.startsWith("RC")) {
				char digit = lastMilestone.charAt(2);
				if (digit == '4') {
					// RC4 is the last release candidate milestone
					milestoneName = "R3_"+version;
				} else {
					milestoneName = "RC" +((char)(digit+1));
				}
			} else if (lastMilestone.startsWith("R3_"+version+"-")) {
				milestoneName = "R3_" + version + "_1";
			} else if (lastMilestone.startsWith("R3_"+version+"_")) {
				char digit = lastMilestone.charAt(5);
				milestoneName = "R3_" + version + "_" + ((char)(digit+1));
			} else {
				MessageDialog.openError(getShell(), DB_Results.getDbTitle(), "Unexpected last milestone name: "+lastMilestone+"!");
				return;
			}

			// Verify the date of the new milestone
			int lastMilestoneDash = lastMilestone.indexOf('-');
			final String lastMilestoneDate = lastMilestone.substring(lastMilestoneDash+1);
			if (milestoneDate.compareTo(lastMilestoneDate) <= 0) {
				// TODO improve this verification
				MessageDialog.openError(getShell(), DB_Results.getDbTitle(), "Milestone "+milestoneDate+" should be after the last milestone: "+lastMilestoneDate+"!");
				return;
			}
		}

		// Verification are ok, ask to add the milestone
		final String milestone = milestoneName + "-" + milestoneDate;
		if (MessageDialog.openConfirm(getShell(), DB_Results.getDbTitle(), milestoneDate+" is a valid milestone date.\n\nDo you want to add the milestone '"+milestone+"' to the preferences?")) {
			this.milestonesCombo.add(milestone);
			this.milestonesCombo.setText("");
		}
	}
}


/**
 * @param milestone
 */
void openMilestoneErrorMessage(String milestone) {
	MessageDialog.openError(getShell(), DB_Results.getDbTitle(), milestone+" is an invalid milestone name. Only 'Mx-yyyymmddHHMM' or 'RCx-yyyymmddHHMM' are accepted!");
}

/*
 * (non-Javadoc) Method declared on PreferencePage
 */
protected void performDefaults() {
	super.performDefaults();
	initializeDefaults();
}

/*
 * (non-Javadoc) Method declared on PreferencePage
 */
public boolean performOk() {
	storeValues();
	try {
		IEclipsePreferences preferences = new InstanceScope().getNode(PLUGIN_ID);
		preferences.flush();
	} catch (BackingStoreException e) {
		e.printStackTrace();
		return false;
	}
	return true;
}

/**
 * Stores the values of the controls back to the preference store.
 */
private void storeValues() {
	IPreferenceStore store = getPreferenceStore();

	// Set version
//	String version;
//	if (this.maintenanceVersion.getSelection()) {
//		version = ECLIPSE_MAINTENANCE_VERSION;
//	} else {
//		version = ECLIPSE_DEVELOPMENT_VERSION;
//	}

	// Set database location
	String location = this.databaseLocationCombo.getText();
	store.setValue(PRE_DATABASE_LOCATION, location);
	int count = this.databaseLocationCombo.getItemCount();
	for (int i=0; i<count; i++) {
		String item = this.databaseLocationCombo.getItem(i);
		if (item.equals(location)) {
			this.databaseLocationCombo.remove(i);
			break;
		}
	}
	this.databaseLocationCombo.add(location, 0);
	for (int i=0; i<count; i++) {
		String item = this.databaseLocationCombo.getItem(i);
		if (item.length() == 0) break;
		store.setValue(PRE_DATABASE_LOCATION+"."+i, item);
	}

	// Set milestones
	char last = location.charAt(location.length()-1);
	String version = last == '5' ? ".v35." : ".v36.";
	count  = this.milestonesCombo.getItemCount();
	for (int i=0; i<count; i++) {
		store.putValue(PRE_MILESTONE_BUILDS + version + i, this.milestonesCombo.getItem(i));
	}
	Util.setMilestones(this.milestonesCombo.getItems());

	// Set default dimension
	String defaultDimension = this.defaultDimensionCombo.getText();
	store.putValue(PRE_DEFAULT_DIMENSION, defaultDimension);
	DB_Results.setDefaultDimension(defaultDimension);

	// Set generated dimensions
	int[] indices = this.resultsDimensionsList.getSelectionIndices();
	int length = indices.length;
	String[] dimensions = new String[length];
	if (length > 0) {
		for (int i = 0; i < indices.length; i++) {
			dimensions[i] = this.resultsDimensionsList.getItem(indices[i]);
			store.putValue(PRE_RESULTS_DIMENSION + "." + i, dimensions[i]);
		}
	}
	DB_Results.setResultsDimensions(dimensions);

	// Set config descriptors
	TableItem[] items = this.configDescriptorsTable.getItems();
	length = items.length;
	for (int i = 0; i < length; i++) {
		TableItem item = items[i];
		store.putValue(PRE_CONFIG_DESCRIPTOR_NAME + "." + i, item.getText(0));
		store.putValue(PRE_CONFIG_DESCRIPTOR_DESCRIPTION + "." + i, item.getText(1));
	}
}

/**
 * (non-Javadoc) Method declared on SelectionListener
 */
public void widgetDefaultSelected(SelectionEvent event) {
}

/**
 * (non-Javadoc) Method declared on SelectionListener
 */
public void widgetSelected(SelectionEvent event) {

	// As for directory when 'Local' button is pushed
	if (event.getSource() == this.localButton) {
		String location = this.databaseLocationCombo.getText();
		if (location.length() == 0 || location.startsWith("net://")) {
			location = DEFAULT_LOCAL_DATA_DIR.substring(0, DEFAULT_LOCAL_DATA_DIR.lastIndexOf(File.separatorChar));
			int count = this.databaseLocationCombo.getItemCount();
			for (int i = 0; i < count; i++) {
				String item = this.databaseLocationCombo.getItem(i);
				if (item.length() == 0) { // nothing in the combo-box list
					break;
				}
				if (!item.startsWith("net://")) {
					location = item;
					break;
				}
			}
		}
		String path = getDirectoryPath(location);
		if (path != null) {
			this.databaseLocationCombo.setText(path);
			this.databaseLocationCombo.add(path);
		}
	}

	// Reset dabase location when 'Releng' button is pushed
	if (event.getSource() == this.relengButton) {
		this.databaseLocationCombo.setText(DEFAULT_DATABASE_LOCATION);
	}

	// Add default dimension to results if necessary
	if (event.getSource() == this.resultsDimensionsList) {
		String[] resultsDimensions = this.resultsDimensionsList.getSelection();
		int length = resultsDimensions.length;
		String defaultDimension = this.defaultDimensionCombo.getText();
		for (int i = 0; i < length; i++) {
			if (resultsDimensions[i].equals(defaultDimension)) {
				// Default dim is already set as a results dimension, hence nothing has to be done
				return;
			}
		}
		System.arraycopy(resultsDimensions, 0, resultsDimensions = new String[length + 1], 0, length);
		resultsDimensions[length] = defaultDimension;
		this.resultsDimensionsList.setSelection(resultsDimensions);
	}
}
}
