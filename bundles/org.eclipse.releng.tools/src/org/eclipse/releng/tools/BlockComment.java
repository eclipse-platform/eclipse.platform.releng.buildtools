/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.releng.tools;

/**
 * @author droberts
 */
public class BlockComment {

	int start;
	int end;
	private String contents;
	private String copyrightHolder;


	public BlockComment(int commentStartLine, int commentEndLine, String comment) {
		start = commentStartLine;
		end = commentEndLine;
		contents = comment;
	}

	public String getContents() {
		return contents;
	}

	/**
	 * @return boolean
	 */
	public boolean isCopyright() {
		return contents.toLowerCase().contains("copyright"); //$NON-NLS-1$
	}

	/**
	 * @return boolean
	 */
	public boolean atTop() {
		return start == 0;
	}

	/**
	 * @return String
	 */
	public String getCopyrightHolder() {
		return copyrightHolder;
	}

}
