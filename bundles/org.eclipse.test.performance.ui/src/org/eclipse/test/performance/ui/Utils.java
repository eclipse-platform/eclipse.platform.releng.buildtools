package org.eclipse.test.performance.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import java.io.BufferedOutputStream;
import junit.framework.AssertionFailedError;

import org.apache.tomcat.core.IncludedResponse;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.TimeSeries;
import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Variations;

/**
 * a utility class which provides html templates and other constant values.
 */
public class Utils {

	public static int getBuildNameIndex(String[] timeSeriesLabels,
			String buildId) {
		for (int i = 0; i < timeSeriesLabels.length; i++) {
			String timeSeriesLabel = timeSeriesLabels[i];
			if (timeSeriesLabel.equals(buildId))
				return i;
		}
		return -1;
	}
	public static boolean inIncludedBuilds(int [] indeces, int i){
		for (int x=0;x<indeces.length;x++){
			if (indeces[x]==i)
				return true;
		}
		return false;
	}
	
	public static int[] getLastSevenNightlyBuildNameIndeces(String[] timeSeriesLabels, String current) {
		int currentIndex=getBuildNameIndex(timeSeriesLabels,current);
		int[] indeces = { -1, -1, -1, -1, -1, -1, -1 };
		int j=6;
		
			for (int i = timeSeriesLabels.length-1; i>-1; i--) {
				if (j==-1)
					break;
				String timeSeriesLabel = timeSeriesLabels[i];
				if (timeSeriesLabel.startsWith("N")&&i<currentIndex){
					indeces[j] = i;
					j--;
				}
			
		}
		return indeces;
	}
	
	/** HTML source used at beginning of html document.
	 */
	public static String HTML_OPEN = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">";

	/**
	 * Closing HTML tag </html>.
	 */
	public static String HTML_CLOSE = "</html>";

	/**
	 * Default style-sheet used on eclipse.org
	 */
	public static String HTML_DEFAULT_CSS = "<style type=\"text/css\">"
			+ "p, table, td, th {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"
			+ "pre {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n"
			+ "h2 { font-family: arial, helvetica, geneva; font-size: 18pt; font-weight: bold ; line-height: 14px}\n"
			+ "code {  font-family: \"Courier New\", Courier, mono; font-size: 10pt}\n"
			+ "sup {  font-family: arial,helvetica,geneva; font-size: 10px}\n"
			+ "h3 {  font-family: arial, helvetica, geneva; font-size: 14pt; font-weight: bold}\n"
			+ "li {  font-family: arial, helvetica, geneva; font-size: 10pt}\n"
			+ "h1 {  font-family: arial, helvetica, geneva; font-size: 28px; font-weight: bold}\n"
			+ "body {  font-family: arial, helvetica, geneva; font-size: 10pt; clip:   rect(   ); margin-top: 5mm; margin-left: 3mm}\n"
			+ ".indextop { font-size: x-large;; font-family: Verdana, Arial, Helvetica, sans-serif; font-weight: bold}\n"
			+ ".indexsub { font-size: xx-small;; font-family: Arial, Helvetica, sans-serif; color: #8080FF}\n"
			+ "</style>";

	/**
	 * An utility object which stores a name, description and url associated for
	 * a performance configuration.
	 */
	public static class ConfigDescriptor {
		String name;

		String description;

		String url;

		String outputDir;

		/**
		 * 
		 * @param name
		 *            the value specifed for the key config in the
		 *            eclipse.perf.config system.property key value listings.
		 *            ie. relengbuildwin2 (machine name)
		 * @param description
		 *            a meaningful description to further describe the config.
		 *            ie. win32-win32-x86 Sun 1.4.2_06
		 * @param url
		 *            a url to results for this config. Used in hyperlinks from
		 *            graphs or tables.
		 */
		public ConfigDescriptor(String name, String description, String url,
				String outputDir) {
			this.name = name;
			this.description = description;
			this.url = url;
			this.outputDir = outputDir;
		}

	}

	/**
	 * 
	 * @param configDescriptors
	 *            a semi-colon separated listing of config descriptions.<br>
	 *            Uses the following format: name,description, url;name2,
	 *            description2,url2;etc..
	 * @return a mapping of config names to their ConfigDescriptors.
	 */
	public static Hashtable getConfigDescriptors(String configDescriptors) {
		// labelMappings in pairs separated by semi-colon ie.
		// relengbuildwin2,win32-win32-x86;trelenggtk,linux-gtk-x86
		StringTokenizer tokenizer = new StringTokenizer(configDescriptors, ";");
		Hashtable configMap = new Hashtable();

		while (tokenizer.hasMoreTokens()) {
			String labelDescriptor = tokenizer.nextToken();
			int commaIndex = labelDescriptor.indexOf(",");
			String[] elements = labelDescriptor.split(",");
			String output = null;
			if (elements.length == 4)
				output = elements[3];
			ConfigDescriptor descriptor = new ConfigDescriptor(elements[0],
					elements[1], elements[2], output);
			configMap.put(elements[0], descriptor);
		}
		return configMap;
	}

	/**
	 * Get all the scenarios for specified variations
	 * 
	 * @return
	 */
	public static Scenario[] getScenarios(String buildIdPattern,
			String scenarioPattern, String config, String jvm) {
		Dim[] qd = null;
		if (scenarioPattern == null)
			scenarioPattern = "";

		Variations variations = getVariations(buildIdPattern, config, jvm);
		return DB.queryScenarios(variations, scenarioPattern + "%",
				PerformanceTestPlugin.BUILD, qd);
	}

	/**
	 * Creates a Variations object from
	 * 
	 * @param buildIdPattern
	 * @param config
	 * @param jvm
	 */
	public static Variations getVariations(String buildIdPattern,
			String config, String jvm) {
		String buildIdPatterns=buildIdPattern.replace(',','%');
		Variations variations = new Variations();
		variations.put(PerformanceTestPlugin.CONFIG, config);
		variations.put(PerformanceTestPlugin.BUILD, buildIdPatterns);
		variations.put("jvm", jvm);
		return variations;
	}

	/**
	 * returns a list of components as taken from common prefixes to scenario
	 * names
	 * 
	 * @param scenarios
	 * @param summaryEntries
	 * @param componentMapping
	 * @return
	 */
	public static ArrayList getComponentNames(Scenario[] scenarios) {
		ArrayList componentNames = new ArrayList();

		for (int i = 0; i < scenarios.length; i++) {
			String prefix = null;
			Scenario scenario = scenarios[i];
			String scenarioName = scenario.getScenarioName();

			// use part of scenario name prior to .test to identify component
			if (scenarioName.indexOf(".test") != -1) {
				prefix = scenarioName.substring(0, scenarioName
						.indexOf(".test"));
				if (!componentNames.contains(prefix))
					componentNames.add(prefix);
			}
		}
		return componentNames;
	}

	public static String getImageMap(FingerPrint fp) {
		String componentDescription = fp.config;
		if (fp.configDescriptors != null)
			componentDescription = ((Utils.ConfigDescriptor) fp.configDescriptors
					.get(fp.config)).description;
		String areas = fp.bar.getAreas();
		if (areas==null)
			areas="";
		String output = "";
		if (new File(fp.outputDirectory,fp.getOutName()+".gif").exists()) {
			output = "<h4>" + componentDescription + "</h4>";
			output = output.concat("<img src=\"" + fp.getOutName()
					+ ".gif\" usemap=\"#" + fp.outName + "\">"
					+ "<map name=\"" + fp.getOutName() + "\">" + areas
					+ "</map>\n");
		} else {
			output = output.concat("<br><br>There is no fingerprint for "
					+ componentDescription + "<br><br>\n");
		}
		return output;
	}
	

	public static String printScenarioStatusTable(FingerPrint fp) {
		// print the component scenario table beneath the fingerprint
		fp.variations.put("config", "%");
		ScenarioStatusTable sst = new ScenarioStatusTable(fp.variations,
				fp.component + "%", fp.configDescriptors);
		return sst.toString();
	}

	public static void copyFile(URL src, String dest) {
		try {
			InputStream in = new FileInputStream(src.getFile());
			OutputStream out = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 */
	public static LineGraph getLineGraph(Scenario t, String dimensionName,
		String reference, String current) {
		Display display = Display.getDefault();
		Color black = display.getSystemColor(SWT.COLOR_BLACK);
		Color green = display.getSystemColor(SWT.COLOR_DARK_GREEN);

		String scenarioName = t.getScenarioName();
		Dim[] dimensions = t.getDimensions();
		Dim dim=null;
		for (int i = 0; i < dimensions.length; i++) {
			if (dimensions[i].getName().equals(dimensionName))
				dim = dimensions[i];
		}

		LineGraph graph = new LineGraph(scenarioName + ": " + dimensionName,
				dim);
		TimeSeries ts = null;
		try {
			ts = t.getTimeSeries(dim);
			int n = ts.getLength();
			

			if (n > 0) {
				for (int j = 0; j < n; j++) {
					String buildID = ts.getLabel(j);
					double value = ts.getValue(j);
					Color c = buildID.indexOf(reference) >= 0 ? green : black;
					int underscoreIndex = buildID.indexOf('_');
					buildID = (buildID.indexOf('_') == -1) ? buildID : buildID
							.substring(0, underscoreIndex);
					if (c == green || ts.getLabel(j).equals(current))
						graph.addItem(buildID, dim.getDisplayValue(value),
								value, c, true);
					else if(ts.getLabel(j).startsWith("I")||ts.getLabel(j).startsWith("2")||ts.getLabel(j).startsWith("3.0.1")||inIncludedBuilds(getLastSevenNightlyBuildNameIndeces(t.getTimeSeriesLabels(),current),j))
						graph.addItem(buildID, dim.getDisplayValue(value),
								value, c);
				}
			}
		} catch (AssertionFailedError e) {
			// System.err.println("Unable to get result for:
			// "+t.getScenarioName()+" "+ts.toString());
		}
		return graph;
	}

	public static void printLineGraphGif(LineGraph p, String output) {
		File outputFile = new  File(output);
		outputFile.getParentFile().mkdir();
		int GRAPH_WIDTH = 600;
		int GRAPH_HEIGHT = 200;
		Image image = new Image(Display.getDefault(), GRAPH_WIDTH, GRAPH_HEIGHT);
		p.paint(image);

	     /* Downscale to 8 bit depth palette to save to gif */
        ImageData data = Utils.downSample(image);
        ImageLoader il= new ImageLoader();
        il.data= new ImageData[] { data };
		OutputStream out = null;
		try {
			out = new BufferedOutputStream(new FileOutputStream(output));
			il.save(out, SWT.IMAGE_GIF);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			image.dispose();
			if (out != null) {
				try {
					out.close();
				} catch (IOException e1) {
					// silently ignored
				}
			}
		}
	}

	/**
	 * @param p
	 * @param output
	 */
	public static String getImageMap(LineGraph p, String imageSource) {
		String result="";
		String areas = p.getAreas();
		if (areas!=null){
				result=result.concat("<img src=\"" + imageSource+"\" usemap=\"#" + p.fTitle + "\">");
				result=result.concat("<map name=\"" + p.fTitle + "\">");
				result=result.concat(areas);
				result=result.concat("</map>");
		}
		return result;
	}

    /** Downsample Image to 8 bit depth format
     *  so that the resulting image data can
     *  be saved to GIF.
     *  Note.  If the source image contains photo
     *  quality content with more than 256 colours,
     *  resulting data will look very poor.
     */
	static int closest(RGB[] rgbs, int n, RGB rgb) {
		int minDist = 256*256*3;
		int minIndex = 0;
		for (int i = 0; i < n; ++i) {
			RGB rgb2 = rgbs[i];
			int da = rgb2.red - rgb.red;
			int dg = rgb2.green - rgb.green;
			int db = rgb2.blue - rgb.blue;
			int dist = da*da + dg*dg + db*db;
			if (dist < minDist) {
				minDist = dist;
				minIndex = i;
			}
		}
		return minIndex;
	}
    

	
	static class ColorCounter implements Comparable {
		RGB rgb;

		int count;

		public int compareTo(Object o) {
			return ((ColorCounter) o).count - count;
		}
	}

    public static ImageData downSample(Image image) {
        ImageData data = image.getImageData();
        if (!data.palette.isDirect && data.depth <= 8) return data;

        // compute a histogram of color frequencies
        HashMap freq = new HashMap();
        int width = data.width;
        int[] pixels = new int[width];
        int[] maskPixels = new int[width];
        for (int y = 0, height = data.height; y < height; ++y) {
            data.getPixels(0, y, width, pixels, 0);
            for (int x = 0; x < width; ++x) {
                RGB rgb = data.palette.getRGB(pixels[x]);
                ColorCounter counter = (ColorCounter) freq.get(rgb);
                if (counter == null) {
                    counter = new ColorCounter();
                    counter.rgb = rgb;
                    freq.put(rgb, counter);
                }
                counter.count++;
            }
        }

        // sort colors by most frequently used
        ColorCounter[] counters = new ColorCounter[freq.size()];
        freq.values().toArray(counters);
        Arrays.sort(counters);
        
        // pick the most frequently used 256 (or fewer), and make a palette
        ImageData mask = null;
        if (data.transparentPixel != -1 || data.maskData != null) {
            mask = data.getTransparencyMask();
        }
        int n = Math.min(256, freq.size());
        RGB[] rgbs = new RGB[n + (mask != null ? 1 : 0)];
        for (int i = 0; i < n; ++i) rgbs[i] = counters[i].rgb;
        if (mask != null) {
            rgbs[rgbs.length - 1] = data.transparentPixel != -1 ? data.palette.getRGB(data.transparentPixel) : new RGB(255, 255, 255);
        }
        PaletteData palette = new PaletteData(rgbs);

        // create a new image using the new palette:
        //   for each pixel in the old image, look up the best matching 
        //   index in the new palette
        ImageData newData = new ImageData(width, data.height, 8, palette);
        if (mask != null) newData.transparentPixel = rgbs.length - 1;
        for (int y = 0, height = data.height; y < height; ++y) {
            data.getPixels(0, y, width, pixels, 0);
            if (mask != null) mask.getPixels(0, y, width, maskPixels, 0);
            for (int x = 0; x < width; ++x) {
            	if (mask != null && maskPixels[x] == 0) {
            		pixels[x] = rgbs.length - 1;
            	}
                else {
                	RGB rgb = data.palette.getRGB(pixels[x]);
                	pixels[x] = closest(rgbs, n, rgb);
                }
             }
            newData.setPixels(0, y, width, pixels, 0);
        }
        return newData;
	}
    
}
