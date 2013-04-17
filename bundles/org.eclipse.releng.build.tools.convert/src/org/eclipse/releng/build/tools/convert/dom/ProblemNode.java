/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.dom;

public class ProblemNode {

    protected static final String EMPTY = "";         //$NON-NLS-1$

    public boolean                isError;
    public int                    charStart;
    public int                    charEnd;
    public int                    line;
    public String                 id;
    public String                 message;
    public int                    sourceStart;
    public int                    sourceEnd;
    public String                 contextValue;
    public int                    globalProblemNumber;
    public int                    problemNumber;
    public String                 sourceFileName;

    public String                 sourceCodeBefore;
    public String                 sourceCodeAfter;
    public String                 sourceCode;

    public void setSources() {
        if ((sourceStart == -1) || (sourceEnd == -1)) {
            sourceCodeBefore = EMPTY;
            sourceCode = contextValue;
            sourceCodeAfter = EMPTY;
        } else {
            final int length = contextValue.length();
            if (sourceStart < length) {
                sourceCodeBefore = contextValue.substring(0, sourceStart);
                final int end = sourceEnd + 1;
                if (end < length) {
                    sourceCode = contextValue.substring(sourceStart, end);
                    sourceCodeAfter = contextValue.substring(end, length);
                } else {
                    sourceCode = contextValue.substring(sourceStart, length);
                    sourceCodeAfter = EMPTY;
                }
            } else {
                sourceCodeBefore = EMPTY;
                sourceCode = EMPTY;
                sourceCodeAfter = EMPTY;
            }
        }
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(isError ? "ERROR " : "WARNING "); //$NON-NLS-1$//$NON-NLS-2$
        buffer.append("line : ").append(line).append(" message = ").append(message);//$NON-NLS-1$//$NON-NLS-2$
        return buffer.toString();
    }
}
