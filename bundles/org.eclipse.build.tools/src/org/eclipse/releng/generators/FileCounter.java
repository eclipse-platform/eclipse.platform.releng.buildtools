
package org.eclipse.releng.generators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This task will count the number of fils in a given directory
 * that match a given filter.  The number of fils will be output
 * to a given output file.  The output file will be overwritten
 * if it already exists.
 * 
 * Note: Filter comparison is NOT case sensitive.  Do not use wild cards.
 * ie .zip counts all files with .zip anywere in the name.
 */
public class FileCounter extends Task {
	
	private String sourceDirectory = "";
	private String filterString = ".zip";
	private String outputFile = "";
	
	public static void main(String args[]) {
		// For testing only.
		FileCounter aFileCounter = new FileCounter();
		aFileCounter.setSourceDirectory("c:\\RelEng\\dean");
		aFileCounter.setOutputFile("c:\\RelEng\\dean\\files.count");
		aFileCounter.setFilterString(".zip");
		aFileCounter.execute();
	}

	public void execute() throws BuildException {
		// Do the work.
		
		int count = 0;
		
		System.out.println("Source Directory: " + this.getSourceDirectory());
		System.out.println("Output File: " + this.getOutputFile());
		System.out.println("Filter String: " + this.getFilterString());
		
		File aDirectory = new File(this.getSourceDirectory());
		if (aDirectory == null) {
			throw new BuildException("Directory " + this.getSourceDirectory() + " not found.");
		}
		
		String[] names = aDirectory.list();
		if (names == null) {
			throw new BuildException("Directory " + this.getSourceDirectory() + " not found.");
		}			
		
		System.out.println("List size: " + names.length);
		
		for (int i = 0; i < names.length; i++) {
			System.out.println("Name: " + names[i]);
			int index = names[i].toLowerCase().indexOf(this.getFilterString().toLowerCase());
			if (index != -1) {
				count++;
			}
		}
		
		try {
			FileOutputStream anOutputStream = new FileOutputStream(this.getOutputFile());
			anOutputStream.write(String.valueOf(count).getBytes());
			anOutputStream.close();
		} catch (FileNotFoundException e) {
			throw new BuildException("Can not create file.count file");
		} catch (IOException e) {
			throw new BuildException("Can not create file.count file");
		}
		
	}

	/**
	 * Gets the sourceDirectory.
	 * @return Returns a String
	 */
	public String getSourceDirectory() {
		return sourceDirectory;
	}

	/**
	 * Sets the sourceDirectory.
	 * @param sourceDirectory The sourceDirectory to set
	 */
	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	/**
	 * Gets the filterString.
	 * @return Returns a String
	 */
	public String getFilterString() {
		return filterString;
	}

	/**
	 * Sets the filterString.
	 * @param filterString The filterString to set
	 */
	public void setFilterString(String filterString) {
		this.filterString = filterString;
	}

	/**
	 * Gets the outputFile.
	 * @return Returns a String
	 */
	public String getOutputFile() {
		return outputFile;
	}

	/**
	 * Sets the outputFile.
	 * @param outputFile The outputFile to set
	 */
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}