package org.eclipse.test.performance.ui;

import java.util.ArrayList;

import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.Variations;

public class Main {

	private String baseline;
	private String dimensionHistoryOutput;
	private String dimensionHistoryUrl;
	private String fpOutput;
	private String fpLinkUrl;
	private String config;
	private String configProperties;
	private String currentBuildId;
	private Variations variations;
	private Scenario[] scenarios;
	private String jvm;
	private String scenarioFilter;
	private boolean genFingerPrints = false;
	private boolean genDimensionGraphs = false;
	private boolean genDimensionHistories = false;
	private boolean genAll = true;

	public static void main(String args[]) {
		Main main = new Main();
		main.parse(args);
		main.run();
	}

	private void run() {
		scenarios = Utils.getScenarios("%", scenarioFilter, config, jvm);
		variations = Utils.getVariations("%", config, jvm);
		
		if (genDimensionHistories || genAll){
			System.out.print("Generating dimension history tables...");
			new DimensionHistories(scenarios, dimensionHistoryOutput, baseline);
			System.out.println("done.");
		}
		
		if (genDimensionGraphs || genAll){
			System.out.print("Generating dimension line graphs...");
			new DimensionsTables(scenarios, baseline, dimensionHistoryOutput+"/graphs");
			System.out.println("done.");
		}
		
		if (genFingerPrints || genAll) {
			System.out.print("Generating fingerprints...");
			new FingerPrint(null, config, baseline, currentBuildId, variations,
					fpOutput, configProperties, fpLinkUrl);
			
			ArrayList components = Utils.getComponentNames(scenarios);
			for (int i = 0; i < components.size(); i++) {
				new FingerPrint(components.get(i).toString(), config, baseline,
						currentBuildId, variations, fpOutput, configProperties,
						fpLinkUrl);
			}
			System.out.println("done.");
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
			if (arg.equals("-fp.link.url")) {
				fpLinkUrl = args[i + 1];
				if (fpLinkUrl.startsWith("-")) {
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
				config = args[i + 1];
				if (config.startsWith("-")) {
					printUsage();
				}
				i++;
			}
			if (arg.equals("-config.properties")) {
				configProperties = args[i + 1];
				if (configProperties.startsWith("-")) {
					printUsage();
				}
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
		if (baseline == null || dimensionHistoryOutput == null
				|| dimensionHistoryUrl == null || fpOutput == null
				|| fpLinkUrl == null || config == null
				|| configProperties == null || jvm == null || currentBuildId == null)
			printUsage();

	}

	private void printUsage() {
		System.out
				.println("Usage:\n"
						+ " -baseline <baseline build id>\n"
						+ " -jvm <jvm name>\n"
						+ " -dim.history.output <path to output directory for html tables of measurements.  Line graphs produced in subdirectory called graphs>\n"
						+ " -dim.history.url <url corresponding to above>\n"
						+ " -fp.output <path to output directory for fingerprint jpegs and html pages>\n"
						+ " -fp.link.url <url to use in bar hyperlinks>\n"
						+ " -config <machine name>\n"
						+ " -config.properties <name1,description1,url1;name2,description2,url2;etc..>\n"
						+ " -current <current build id>\n"
						+ " [-scenario.filter <scenario prefix>]\n"
						+ " [-fingerprints]\n"
						+ " [-dimensiongraphs]\n"
						+ " [-dimensiontables]\n");
		System.exit(1);

	}

}