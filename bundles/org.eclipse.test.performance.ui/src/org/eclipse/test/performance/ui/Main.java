/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.test.performance.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.test.internal.performance.results.db.DB_Results;

/**
 * Main class to generate performance results of all scenarios matching a given
 * pattern in one HTML page per component.
 *
 * @see GenerateResults for the complete implementation
 */
public class Main implements IApplication {

    /**
     * Generate the performance results for a specified build regarding to a
     * specific reference. This action generates following HTML files:
     * <ul>
     * <li>A summary table to see the variations for all the concerned scenarios
     * </li>
     * <li>A global php file including global scenario fingerprints and links
     * for all concerned components results php files</li>
     * <li>A php file for each component including scenario fingerprints and
     * status table with links to a scenario data file</li>
     * <li>A data HTML file for each config of each scenario included in status
     * table</li>
     * </ul>
     *
     * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
     */
    @Override
    public Object start(IApplicationContext context) throws Exception {
        DB_Results.DB_CONNECTION = true; // force DB connection while running
                                         // the application
        GenerateResults generation = new GenerateResults();
        String[] args = (String[]) context.getArguments().get("application.args");
        if (args.length > 0) {
            System.out.println("\n\t= = Raw arguments ('application.args') passed to performance ui application: = =");
            for (int i = 0; i < args.length; i++) {
                System.out.println("\t\t>" + args[i] + "<");
            }
        }
        IStatus exitStatus = generation.run(args);
        return exitStatus;
    }

    @Override
    public void stop() {
        // Do nothing
    }
}
