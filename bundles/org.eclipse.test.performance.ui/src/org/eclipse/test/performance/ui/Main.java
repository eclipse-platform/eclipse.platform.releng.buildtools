package org.eclipse.test.performance.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.Variations;

public class Main {

	private String baseline;
	private String dimensionHistoryOutput;
	private String dimensionHistoryUrl;
	private String fpOutput;
	private String[] config;
	private Hashtable configDescriptors;
	private String currentBuildId;
	private Variations variations;
	private Scenario[] scenarios;
	private String jvm;
	private String scenarioFilter;
	private boolean genFingerPrints = false;
	private boolean genDimensionGraphs = false;
	private boolean genDimensionHistories = false;
	private boolean genAll = true;
	private Hashtable fingerPrints = new Hashtable();

	public static void main(String args[]) {
		Main main = new Main();
		main.parse(args);
		main.run();
	}

	private void run() {

		for (int i = 0; i < config.length; i++) {
			Utils.ConfigDescriptor cd = (Utils.ConfigDescriptor) configDescriptors
					.get(config[i]);
			dimensionHistoryUrl = cd.url;
			if (cd.outputDir != null)
				dimensionHistoryOutput = cd.outputDir;
			//TODO make this work occur in parallel
			new Worker(config[i]);
		}

		// print area maps for each fingerprint
		Enumeration components = fingerPrints.keys();

		// print fingerprint/scenario status pages
		while (components.hasMoreElements()) {
			String component = components.nextElement().toString();
			try {
				File output = new File(fpOutput + '/' + component + ".php");
				output.getParentFile().mkdirs();
				PrintStream os = new PrintStream(new FileOutputStream(fpOutput
						+ '/' + component + ".php"));
				os.println(Utils.HTML_OPEN);
				os.println(Utils.HTML_DEFAULT_CSS);
				os.println("<body>");
				Hashtable fps = (Hashtable) fingerPrints.get(component);
				Enumeration configs = fps.keys();
				String title = "<h3>Performance of " + component + ": "
						+ currentBuildId + " relative to"
						+ baseline.substring(0, baseline.indexOf("_"))
						+ "</h3>";
				if (component.equals("global"))
					title = "<h3>Performance of " + currentBuildId
							+ " relative to"
							+ baseline.substring(0, baseline.indexOf("_"))
							+ "</h3>";
				os.println(title);
				while (configs.hasMoreElements()) {
					String config = configs.nextElement().toString();
					FingerPrint fp = (FingerPrint) fps.get(config);
					os.println(Utils.printHtmlFingerPrint(fp));
				}
				if (component != "") {
					char buildType = currentBuildId.charAt(0);

					// print the component scenario table beneath the
					// fingerprint
					variations.put("config", "%");
					ScenarioStatusTable sst = new ScenarioStatusTable(
							variations, component + "%", configDescriptors);
					os.println(sst.toString());
				}

				os.println(Utils.HTML_CLOSE);
				os.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private class Worker{
		String config;

		Worker(String config) {
			this.config = config;
			run();
		}

		public void run() {

			scenarios = Utils.getScenarios("%", scenarioFilter, config, jvm);
			variations = Utils.getVariations("%", config, jvm);

			if (genDimensionGraphs || genAll) {
				System.out.print(config
						+ ": generating dimension history graphs...");
				new DimensionHistories(scenarios, dimensionHistoryOutput
						+ "/graphs", baseline);
				System.out.println("done.");
			}

			if (genDimensionHistories || genAll) {
				System.out.print(config
						+ ": generating dimension line tables...");
				new DimensionsTables(scenarios, baseline,
						dimensionHistoryOutput);
				System.out.println("done.");
			}

			if (genFingerPrints || genAll) {
				System.out.print(config + ": generating fingerprints...");
				FingerPrint global = new FingerPrint(null, config, baseline,
						currentBuildId, variations, fpOutput, configDescriptors);
				Hashtable t;
				if (fingerPrints.get("global") != null)
					t = (Hashtable) fingerPrints.get("global");
				else
					t = new Hashtable();

				t.put(config, global);
				fingerPrints.put("global", t);

				ArrayList components = Utils.getComponentNames(scenarios);
				for (int i = 0; i < components.size(); i++) {
					String component = components.get(i).toString();
					variations.put("config", config);
					FingerPrint componentFp = new FingerPrint(component,
							config, baseline, currentBuildId, variations,
							fpOutput, configDescriptors);
					if (fingerPrints.get(component) != null)
						t = (Hashtable) fingerPrints.get(component);
					else
						t = new Hashtable();
					t.put(config, componentFp);
					fingerPrints.put(component, t);
				}
				System.out.println("done.");
			}
		}
	}

	private void parse(String args[]) {
		int i = 0;
		if (args.length == 0) {
			printUsage();
		}

		while (i < args.length) {
			String arg = args[i];
			if (arg.equals("-baseline")) {
				baseline = args[i + 1];
				if (baseline.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-current")) {
				currentBuildId = args[i + 1];
				if (currentBuildId.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-jvm")) {
				jvm = args[i + 1];
				if (jvm.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-fp.output")) {
				fpOutput = args[i + 1];
				if (fpOutput.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-dim.history.output")) {
				dimensionHistoryOutput = args[i + 1];
				if (dimensionHistoryOutput.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-dim.history.url")) {
				dimensionHistoryUrl = args[i + 1];
				if (dimensionHistoryUrl.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-config")) {
				String configs = args[i + 1];
				if (configs.startsWith("-")) {
					printUsage();
				}
				config = configs.split(",");
				i++;
			}
			if (arg.equals("-config.properties")) {
				String configProperties = args[i + 1];
				if (configProperties.startsWith("-")) {
					printUsage();
				}
				configDescriptors = Utils
						.getConfigDescriptors(configProperties);
				i++;
			}
			if (arg.equals("-scenario.filter")) {
				scenarioFilter = args[i + 1];
				if (scenarioFilter.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-fingerprints")) {
				genFingerPrints = true;
				genAll = false;
			}
			if (arg.equals("-dimensiongraphs")) {
				genDimensionGraphs = true;
				genAll = false;
			}
			if (arg.equals("-dimensionhistories")) {
				genDimensionHistories = true;
				genAll = false;
			}
			i++;
		}
		if (baseline == null || fpOutput == null || config == null
				|| configDescriptors == null || jvm == null
				|| currentBuildId == null)
			printUsage();

	}

	private void printUsage() {
		System.out
				.println("Usage:\n"
						+ " -baseline <baseline build id>\n"
						+ " -jvm <jvm name>\n"
						+ " -dim.history.output <path to output directory for html tables of measurements.  Optional if information provided in config.properties.  Line graphs produced in subdirectory called graphs>\n"
						+ " -fp.output <path to output directory for fingerprint jpegs and html pages>\n"
						+ " -config <comma separated list of config names>\n"
						+ " -config.properties <name1,description1,url1 [,outputdir1];name2,description2,url2 [,outputdir2];etc..>\n"
						+ " -current <current build id>\n"
						+ " [-scenario.filter <scenario prefix>]\n"
						+ " [-fingerprints]\n" 
						+ " [-dimensiongraphs]\n"
						+ " [-dimensiontables]\n");
		System.exit(1);

	}

}