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
import java.util.Map;

public interface IDOMConverter {

    void dump(int formatVersion, Map<String, String> options, LogDocumentNode documentNode);

    String getUnderLine(String sourceBefore, String sourceOfError);

    void writeAnchorsReferences(Writer writer) throws IOException;

    void writeAnchorsReferencesDiscouragedRulesWarnings(Writer writer) throws IOException;

    void writeAnchorsReferencesErrors(Writer writer) throws IOException;

    void writeAnchorsReferencesForbiddenRulesWarnings(Writer writer) throws IOException;

    void writeAnchorsReferencesOtherWarnings(Writer writer) throws IOException;

    void writeDiscouragedRulesWarningsAnchor(Writer writer) throws IOException;

    void writeErrorAnchor(Writer writer) throws IOException;

    void writeForbiddenRulesWarningsAnchor(Writer writer) throws IOException;

    void writeOtherWarningsAnchor(Writer writer) throws IOException;

    void writeTopAnchor(Writer writer) throws IOException;
}
