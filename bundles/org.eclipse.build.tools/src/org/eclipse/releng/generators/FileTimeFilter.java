package org.eclipse.releng.generators;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

/**
 * @author droberts
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class FileTimeFilter implements FileFilter {
	
	private long age;
	private long time;
	
	public FileTimeFilter() {
		this.time = new Date().getTime();
	}
	
	/**
	 * Constructor DirectoryTimeFilter.
	 * @param i
	 */
	public FileTimeFilter(long i) {
		super();
		this.time = new Date().getTime();
		this.age = i;
	}


	/**
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File pathname) {
		
		long lastModified = pathname.lastModified();
		return (time > lastModified + age);
	}

	/**
	 * Returns the age.
	 * @return int
	 */
	public long getAge() {
		return age;
	}

	/**
	 * Sets the age.
	 * @param age The age to set
	 */
	public void setAge(long age) {
		this.age = age;
	}
}
