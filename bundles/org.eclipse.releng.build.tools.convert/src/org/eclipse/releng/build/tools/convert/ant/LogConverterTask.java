/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng.build.tools.convert.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * This ant task is used to convert all xml files inside the given <code>logDirectory</code> and
 * into html or txt files.
 *
 * <p>The resulting files are written in the same directory than the corresponding xml file.</p>
 *
 * <p>This is not intended to be subclassed by users.</p>
 */
public class LogConverterTask extends Task {

	private String input;

	private boolean validation;

	public String getInput() {
		return this.input;
	}

	public void setInput(String logDirectory) {
		this.input = logDirectory;
	}

	public boolean isValidation() {
		return this.validation;
	}

	public void setValidation(boolean validation) {
		this.validation = validation;
	}

	public void execute() throws BuildException {
		if (this.input == null) {
			throw new BuildException("No input is specified");//$NON-NLS-1$
		}
		try {
			if (isValidation()) {
				Converter.run(new String[] {"-v", "-r", "-i", this.input}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			} else {
				Converter.run(new String[] {"-r", "-i", this.input}); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (Exception e) {
			throw new BuildException("Exception during conversion", e);//$NON-NLS-1$
		}
	}
}
