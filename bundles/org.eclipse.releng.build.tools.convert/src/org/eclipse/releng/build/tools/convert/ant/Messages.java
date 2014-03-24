/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.ant;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {

    private final ResourceBundle resourceBundle;

    public Messages(final String bundleName) {
        resourceBundle = ResourceBundle.getBundle(bundleName);
    }

    public String getString(final String key) {
        try {
            return resourceBundle.getString(key);
        }
        catch (final MissingResourceException e) {
            return '!' + key + '!';
        }
    }
}
