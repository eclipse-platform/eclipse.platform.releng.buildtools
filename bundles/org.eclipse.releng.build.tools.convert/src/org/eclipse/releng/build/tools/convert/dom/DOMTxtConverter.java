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

import java.io.IOException;
import java.io.Writer;

import org.eclipse.releng.build.tools.convert.ant.Messages;

public class DOMTxtConverter extends AbstractDOMConverter {

	public DOMTxtConverter() {
		this.messages = new Messages("org.eclipse.releng.build.tools.convert.ant.txt_messages"); //$NON-NLS-1$
	}

	public String getUnderLine(String sourceBefore, String sourceOfError) {
		StringBuffer buffer = new StringBuffer();
		char[] chars = sourceBefore.toCharArray();
		for (int i = 0, max = chars.length; i < max; i++) {
			switch(chars[i]) {
				case '\t' :
					buffer.append('\t');
					break;
				default:
					buffer.append(' ');
			}
		}
		chars = sourceOfError.toCharArray();
		for (int i = 0, max = chars.length; i < max; i++) {
			buffer.append('^');
		}
		return String.valueOf(buffer);
	}

	public void writeAnchorsReferences(Writer writer) throws IOException {
		// do nothing
	}

	public void writeAnchorsReferencesErrors(Writer writer) throws IOException {
		// do nothing
	}

	public void writeAnchorsReferencesOtherWarnings(Writer writer) throws IOException {
		// do nothing
	}

	public void writeAnchorsReferencesForbiddenRulesWarnings(Writer writer) throws IOException {
		// do nothing
	}

	public void writeAnchorsReferencesDiscouragedRulesWarnings(Writer writer) throws IOException {
		// do nothing
	}

	public void writeErrorAnchor(Writer writer) throws IOException {
		// do nothing
	}

	public void writeOtherWarningsAnchor(Writer writer) throws IOException {
		// do nothing
	}

	public void writeForbiddenRulesWarningsAnchor(Writer writer) throws IOException {
		// do nothing
	}

	public void writeDiscouragedRulesWarningsAnchor(Writer writer) throws IOException {
		// do nothing
	}

	public void writeTopAnchor(Writer writer) throws IOException {
		// do nothing
	}
}
