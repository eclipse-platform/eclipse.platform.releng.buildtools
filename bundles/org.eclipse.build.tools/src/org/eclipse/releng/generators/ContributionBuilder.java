package org.eclipse.releng.generators;

/**
 * This application writes a single file containg build entries from a number of
 * .map files
 */

import java.io.*;
import java.util.*;
import org.apache.tools.ant.Task;

public class ContributionBuilder extends Task {

	//stores each line read in from a build input
	private Vector entries;

	//stores entries with versions replaced by HEAD
	private Vector bufferedEntries;

	// absolute path to directory containing all build contribution directories
	private String containerDirectory;

	// "N" for nightly or "I" for integration build
	private String buildType;

	// absolute path to where full directory.txt should be written
	private String masterDirectory;

	public void setBuildtype(String s) {
		buildType = s;
	}

	public void setMasterdirectory(String s) {
		masterDirectory = s;
	}

	public void setContainerdirectory(String s) {
		containerDirectory = s;
	}

	public ContributionBuilder() {}

	public void getEntries(File file) {
		//find *.map contained in a directory and store entries in a master directory.txt file
		if (file.isDirectory()) {
			for (int i = 0; i < file.listFiles().length; i++) {
				getEntries(file.listFiles()[i]);
			}
		} else {
			if (file.getName().endsWith(".map"))
				read(file);
		}
	}

	private void read(File file) {
		try {
			BufferedReader directoryReader = new BufferedReader(new FileReader(file));
			String str = directoryReader.readLine();
			while (str != null) {
				if (!str.equals("")) {
					entries.add(str);
				}
				str = directoryReader.readLine();
			}
			directoryReader.close();

		} catch (FileNotFoundException e) {
			System.out.println("Unable to find file: " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("Problems reading file: " + file.getAbsolutePath());
		}

	}

	private void write(File directory, String buildType) throws IOException {
		if (directory.exists())
			directory.delete();

		PrintWriter out = new PrintWriter(new FileWriter(directory));

		Enumeration buildEntries = entries.elements();

		if (!buildType.equals("N")) {
			while (buildEntries.hasMoreElements()) {
				String s = (String) buildEntries.nextElement();
				if (s.startsWith("#")) {
					continue;
				}
				out.println(s);
			}
			out.close();
		
		} else {
			writeDirectoryforDailyBuilds(directory, out);
		}
	}
	
	private void writeDirectoryforDailyBuilds(File directory, PrintWriter out){
		boolean useAsIs = false;
		Enumeration buildEntries = entries.elements();
		
		while (buildEntries.hasMoreElements()) {

				String originalEntry = (String) buildEntries.nextElement();
				StringBuffer bufferedEntry = new StringBuffer(originalEntry);

				if (originalEntry.startsWith("!start")) {
					useAsIs = true;
					continue;
				} else if (originalEntry.startsWith("!end")) {
					useAsIs = false;
					continue;
				} else if (originalEntry.startsWith("#")) {
					continue;
				} else {
					if (useAsIs) {
						out.println(originalEntry);
						continue;
					} else {
						int start = originalEntry.indexOf("=");
						int end = originalEntry.indexOf(",", start);

						if (start != -1 && end != -1) {
							String newEntry = new String(bufferedEntry.replace(start + 1, end, "HEAD"));
							out.println(newEntry);
						} else {
							out.println(originalEntry);
						}
					}
				}
			}
			out.close();
		}
	
	
	

	public void execute() {
		ContributionBuilder a = new ContributionBuilder();
		File file = new File(containerDirectory);
		entries = new Vector();
		getEntries(file);

		try {
			write(new File(masterDirectory), buildType);
		} catch (IOException e) {
			System.out.println("Unable to create directory.txt");
		}
	}

	//test
	public static void main(String args[]) {
		ContributionBuilder a = new ContributionBuilder();

		a.setContainerdirectory("D:\\workspaces\\builderfixing\\org.eclipse.releng");
		a.setBuildtype("I");
		a.setMasterdirectory("d://master.txt");
		a.execute();
	}

}