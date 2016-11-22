/*******************************************************************************
 * Copyright (c) 2006, 2016 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.dom;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.releng.build.tools.convert.ant.Messages;

public class DOMTxtConverter extends AbstractDOMConverter {

    public DOMTxtConverter() {
        messages = new Messages("org.eclipse.releng.build.tools.convert.ant.txt_messages"); //$NON-NLS-1$
    }

    @Override
    public String getUnderLine(final String sourceBefore, final String sourceOfError) {
        final StringBuffer buffer = new StringBuffer();
        char[] chars = sourceBefore.toCharArray();
        for (int i = 0, max = chars.length; i < max; i++) {
            switch (chars[i]) {
                case '\t':
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

    @Override
    public void writeAnchorsReferences(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeAnchorsReferencesDiscouragedRulesWarnings(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeAnchorsReferencesErrors(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeAnchorsReferencesForbiddenRulesWarnings(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeAnchorsReferencesOtherWarnings(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeDiscouragedRulesWarningsAnchor(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeErrorAnchor(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeForbiddenRulesWarningsAnchor(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeOtherWarningsAnchor(final Writer writer) throws IOException {
        // do nothing
    }

    @Override
    public void writeTopAnchor(final Writer writer) throws IOException {
        // do nothing
    }
}
