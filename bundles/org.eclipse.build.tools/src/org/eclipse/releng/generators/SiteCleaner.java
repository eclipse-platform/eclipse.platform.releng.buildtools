package org.eclipse.releng.generators;

import java.io.File;
import java.io.FileFilter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * A custome ant task that will delete all child files and directories older
 * than a given number of minutes.
 *
 * The caller must set the directory, the number of minutes as the cut off
 * and they just set realDelete to true.  (If realDelete is not explicitly
 * set then the task will just print a list of the files it would delete and
 * no deletion will occur
 * 
 * The parent directory is not deleted but all contained files *and* directories
 * older than the given number of minutes is.
 */
public class SiteCleaner  extends Task {
	private String directoryName = File.separator + "bogus";  // for protection
	private int ageInMinutes = 2 * 24 * 60;
	private boolean realDelete = false;  // for protection
	
	public static void main(String[] args) {
		// For testing only.
		
		SiteCleaner instance = new SiteCleaner();
		instance.setAgeInMinutes(0);	//18
		instance.setRealDelete(true);
		
		instance.setDirectoryName("d:\\builds\\transfer\\files\\master\\downloads\\drops");
		instance.execute();
		instance.setDirectoryName("d:\\builds\\transfer\\files\\zrh");
		instance.execute();
		instance.setDirectoryName("d:\\builds\\transfer\\files\\snz");
		instance.execute();
	}

	public void execute() throws BuildException {
	
		FileFilter aFilter = new FileTimeFilter(this.getAgeInMinutes() * 60 * 1000);
		File root = new File(getDirectoryName());
		File[] files = root.listFiles(aFilter);
		if (files == null) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (isRealDelete()) {
				delete(file);
			} else {
				testDelete(file);
			}
		}		
	}
	
	/**
	 * Method testDelete.
	 * @param file
	 */
	private void testDelete(File file) {
		System.out.println("Delete: " + file.getPath());
		System.out.println("isDirectory: " + file.isDirectory());
	}

	public static void delete(File file) {
		
		if(!file.exists()) {
			return;
		}
		
		if(file.isDirectory()) {
			String[] children = file.list();
			for(int i = 0; i < children.length; ++i) {
				File child = new File(file, children[i]);
				delete(child);
			}
		}
		file.delete();	
	}
	
	/**
	 * Returns the directoryName.
	 * @return String
	 */
	public String getDirectoryName() {
		return directoryName;
	}

	/**
	 * Sets the directoryName.
	 * @param directoryName The directoryName to set
	 */
	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	/**
	 * Returns the ageInMinutes.
	 * @return int
	 */
	public int getAgeInMinutes() {
		return ageInMinutes;
	}

	/**
	 * Sets the ageInMinutes.
	 * @param ageInMinutes The ageInMinutes to set
	 */
	public void setAgeInMinutes(int ageInMinutes) {
		this.ageInMinutes = ageInMinutes;
	}

	/**
	 * Returns the realDelete.
	 * @return boolean
	 */
	public boolean isRealDelete() {
		return realDelete;
	}

	/**
	 * Sets the realDelete.
	 * @param realDelete The realDelete to set
	 */
	public void setRealDelete(boolean realDelete) {
		this.realDelete = realDelete;
	}

}
