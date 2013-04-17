/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.releng.build.tools.convert.dom;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.releng.build.tools.convert.ant.ConverterFactory;
import org.eclipse.releng.build.tools.convert.ant.Messages;

public class DOMHtmlConverter extends AbstractDOMConverter {

    public DOMHtmlConverter() {
        //this.messages = new Messages("org.eclipse.releng.build.tools.convert.ant.html_messages"); //$NON-NLS-1$
        messages = new Messages("org.eclipse.releng.build.tools.convert.ant.html_messages"); //$NON-NLS-1$
    }

    public String getUnderLine(final String sourceBefore, final String sourceOfError) {
        return ConverterFactory.EMPTY;
    }

    public void writeAnchorsReferences(final Writer writer) throws IOException {
        writer.write(messages.getString("anchors.references.no_top"));//$NON-NLS-1$
    }

    public void writeAnchorsReferencesDiscouragedRulesWarnings(final Writer writer) throws IOException {
        writer.write(messages.getString("anchors.references.no_discouraged_warnings"));//$NON-NLS-1$
    }

    public void writeAnchorsReferencesErrors(final Writer writer) throws IOException {
        writer.write(messages.getString("anchors.references.no_errors"));//$NON-NLS-1$
    }

    public void writeAnchorsReferencesForbiddenRulesWarnings(final Writer writer) throws IOException {
        writer.write(messages.getString("anchors.references.no_forbidden_warnings"));//$NON-NLS-1$
    }

    public void writeAnchorsReferencesOtherWarnings(final Writer writer) throws IOException {
        writer.write(messages.getString("anchors.references.no_other_warnings"));//$NON-NLS-1$
    }

    public void writeDiscouragedRulesWarningsAnchor(final Writer writer) throws IOException {
        writer.write(messages.getString("discouraged_warnings.title_anchor"));//$NON-NLS-1$
    }

    public void writeErrorAnchor(final Writer writer) throws IOException {
        writer.write(messages.getString("errors.title_anchor"));//$NON-NLS-1$
    }

    public void writeForbiddenRulesWarningsAnchor(final Writer writer) throws IOException {
        writer.write(messages.getString("forbidden_warnings.title_anchor"));//$NON-NLS-1$
    }

    public void writeOtherWarningsAnchor(final Writer writer) throws IOException {
        writer.write(messages.getString("other_warnings.title_anchor"));//$NON-NLS-1$
    }

    public void writeTopAnchor(final Writer writer) throws IOException {
        writer.write(messages.getString("problem.summary.title_anchor"));//$NON-NLS-1$
    }
}
