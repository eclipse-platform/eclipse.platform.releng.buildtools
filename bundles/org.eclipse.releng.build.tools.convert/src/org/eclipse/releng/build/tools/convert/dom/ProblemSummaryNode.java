/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.dom;

public class ProblemSummaryNode {

    public int numberOfProblems;
    public int numberOfErrors;
    public int numberOfWarnings;
    public int numberOfInfos;

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("problems : ") //$NON-NLS-1$
                .append(numberOfProblems).append(" errors : ") //$NON-NLS-1$
                .append(numberOfErrors).append(" warnings : ") //$NON-NLS-1$
                .append(numberOfWarnings).append(" infos : ") //$NON_NLS-1$
                .append(numberOfInfos);
        return buffer.toString();
    }
}
