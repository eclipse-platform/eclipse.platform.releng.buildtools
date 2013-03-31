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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
	private final ResourceBundle resourceBundle;

	public Messages(String bundleName) {
		this.resourceBundle = ResourceBundle.getBundle(bundleName);
	}

	public String getString(String key) {
		try {
			return this.resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
