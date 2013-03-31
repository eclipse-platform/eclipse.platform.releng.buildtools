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
package org.eclipse.releng.build.tools.convert.dom;

public class ProblemNode {
	protected static final String EMPTY = ""; //$NON-NLS-1$

	public boolean isError;
	public int charStart;
	public int charEnd;
	public int line;
	public String id;
	public String message;
	public int sourceStart;
	public int sourceEnd;
	public String contextValue;
	public int globalProblemNumber;
	public int problemNumber;
	public String sourceFileName;

	public String sourceCodeBefore;
	public String sourceCodeAfter;
	public String sourceCode;

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.isError ? "ERROR " : "WARNING "); //$NON-NLS-1$//$NON-NLS-2$
		buffer.append("line : ").append(this.line).append(" message = ").append(this.message);//$NON-NLS-1$//$NON-NLS-2$
		return buffer.toString();
	}

	public void setSources() {
		if (this.sourceStart == -1 || this.sourceEnd == -1) {
			this.sourceCodeBefore = EMPTY;
			this.sourceCode = this.contextValue;
			this.sourceCodeAfter = EMPTY;
		} else {
			int length = this.contextValue.length();
			if (this.sourceStart < length) {
				this.sourceCodeBefore = this.contextValue.substring(0, this.sourceStart);
				int end = this.sourceEnd + 1;
				if (end < length) {
					this.sourceCode = this.contextValue.substring(this.sourceStart, end);
					this.sourceCodeAfter = this.contextValue.substring(end, length);
				} else {
					this.sourceCode = this.contextValue.substring(this.sourceStart, length);
					this.sourceCodeAfter = EMPTY;
				}
			} else {
				this.sourceCodeBefore = EMPTY;
				this.sourceCode = EMPTY;
				this.sourceCodeAfter = EMPTY;
			}
		}
	}
}
