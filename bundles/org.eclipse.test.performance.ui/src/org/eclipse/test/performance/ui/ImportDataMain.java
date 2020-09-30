/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Paul Pazderski - initial API and implementation
 *******************************************************************************/

package org.eclipse.test.performance.ui;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.test.internal.performance.data.Sample;
import org.eclipse.test.internal.performance.db.Variations;

/**
 * Helper application for Eclipse releng process. Instead of writing performance test data into a central database they are now
 * serialized into a file. This application take the part to deserialize the results from file and store them in a local database
 * for the following result generation step.
 * <p>
 * The data files to import are given as command line arguments and will be deleted upon successful import into database.
 * </p>
 */
public class ImportDataMain implements IApplication {

  @Override
  public Object start(IApplicationContext context) throws Exception {
    String[] args = (String[]) context.getArguments().get("application.args");
    if (args.length > 0) {
      System.out.println("\n\t= = Raw arguments ('application.args') passed to performance import application: = =");
      for (String arg : args) {
        System.out.println("\t\t>" + arg + "<");
      }
    }
    IStatus result = run(args);
    (result.isOK() ? System.out : System.err).println(result.getMessage());
    if (result.getException() != null) {
      result.getException().printStackTrace();
    }
    return result;
  }

  private IStatus run(String[] args) {
    try {
      // parse arguments
      List<Path> inputFiles = new ArrayList<>();
      boolean processOpts = true;
      for (String arg : args) {
        if (arg.equals("--")) {
          processOpts = false;
          continue;
        }
        if (processOpts && arg.startsWith("-")) {
          System.err.println("ERROR: Unrecognized option found, with value of >" + arg + "<");
          continue;
        }

        Path inputFile = Paths.get(arg);
        if (Files.isReadable(inputFile)) {
          inputFiles.add(inputFile);
        } else {
          System.err.println("ERROR: invalid input argument. Cannot read file: " + inputFile);
        }
      }

      // check database
      System.out.println("INFO: Connecting to database...");

      // import data
      IStatus exitStatus = new Status(IStatus.OK, UiPlugin.getDefault().toString(), "Nothing to import.");
      System.out.println("INFO: Start importing " + inputFiles.size() + " performance data files.");
      for (Path inputFile : inputFiles) {
        exitStatus = importData(inputFile);
        if (exitStatus.isOK()) {
          Files.delete(inputFile);
        }
      }
      return exitStatus;
    } catch (Exception ex) {
      return new Status(IStatus.ERROR, UiPlugin.getDefault().toString(), "Performance data import failed with exception!", ex);
    }
  }

  @Override
  public void stop() {
    // Do nothing
  }

  /**
   * Import performance test results from the given input file into database.
   *
   * @param inputFile
   *          serialized performance data to import
   * @throws ClassCastException
   *           if the input file has not the expected content but unknown objects
   * @throws IOException
   *           for general problems on reading the input file
   */
  private IStatus importData(Path inputFile) throws IOException, ClassNotFoundException {
    System.out.println("INFO: Reading data from " + inputFile);
    // Note: the input file can contain multiple data entries
    // (ID+Variation+Sample) but they are not written consecutive
    // by one object stream but instead by a new object stream
    // each time. That's why it's required to use one input stream
    // but multiple object streams to read them.
    try (InputStream is = Files.newInputStream(inputFile)) {
      while (true) { // loop will end on input stream EOF
        ObjectInputStream ois = new ObjectInputStream(is);
        String scenarioId = null;
        Variations variations = null;
        Sample sample = null;
        while (scenarioId == null || variations == null || sample == null) {
          Object o = ois.readObject();
          if (String.class.equals(o.getClass())) {
            scenarioId = (String) o;
          } else if (Variations.class.equals(o.getClass())) {
            variations = (Variations) o;
          } else if (Sample.class.equals(o.getClass())) {
            sample = (Sample) o;
          } else {
            System.err.println("WARN: Input contains unexpected object of type " + o.getClass().getCanonicalName());
          }
        }

        System.out.println("DEBUG: Store data for scenario " + scenarioId);
      }
    } catch (EOFException ex) {
      // EOFException is the intended way to end the loop
      return new Status(IStatus.OK, UiPlugin.getDefault().toString(), "Everything is OK");
    }
  }
}
