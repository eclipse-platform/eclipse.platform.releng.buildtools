/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.ant;

import org.eclipse.releng.build.tools.convert.dom.DOMHtmlConverter;
import org.eclipse.releng.build.tools.convert.dom.DOMTxtConverter;
import org.eclipse.releng.build.tools.convert.dom.IDOMConverter;

public class ConverterFactory {

    public static final String EMPTY = ""; //$NON-NLS-1$
    public static final int    TXT   = 0;
    public static final int    HTML  = 1;

    public static IDOMConverter createDOMConverter(final int id) {
        switch (id) {
            case TXT:
                return new DOMTxtConverter();
            case HTML:
                return new DOMHtmlConverter();
            default:
                return new DOMHtmlConverter();
        }
    }
}
